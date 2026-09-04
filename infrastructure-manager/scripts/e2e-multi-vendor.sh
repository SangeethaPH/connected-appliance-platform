#!/usr/bin/env bash
set -euo pipefail

SIMULATOR_URL="${SIMULATOR_URL:-http://localhost:8081}"
INFRA_URL="${INFRA_URL:-http://localhost:8080}"
INFRA_AUTH="${INFRA_AUTH:?Set INFRA_AUTH to username:password}"
VENDOR_USERNAME="${VENDOR_USERNAME:?Set VENDOR_USERNAME}"
VENDOR_PASSWORD="${VENDOR_PASSWORD:?Set VENDOR_PASSWORD}"

for command in curl jq python3; do
  command -v "$command" >/dev/null || { echo "Required command not found: $command"; exit 1; }
done

curl -fsS "$SIMULATOR_URL/actuator/health" >/dev/null
curl -fsS "$INFRA_URL/actuator/health" >/dev/null

create_appliance() {
  local vendor_code="$1" name="$2" type="$3"
  curl -fsS -X POST "$SIMULATOR_URL/api/simulator/appliances" \
    -H 'Content-Type: application/json' \
    -d "{\"vendorId\":\"$vendor_code\",\"name\":\"$name\",\"type\":\"$type\",\"metricIntervalSeconds\":60,\"emissionMode\":\"MANUAL_ONLY\"}"
}

ensure_vendor() {
  local code="$1" name="$2" vendor_id
  vendor_id="$(curl -fsS -u "$INFRA_AUTH" "$INFRA_URL/api/vendors" |
    jq -r --arg code "$code" '.[] | select(.code == $code) | .id' | head -n 1)"
  if [[ -z "$vendor_id" ]]; then
    vendor_id="$(curl -fsS -u "$INFRA_AUTH" -X POST "$INFRA_URL/api/vendors" \
      -H 'Content-Type: application/json' \
      -d "{\"code\":\"$code\",\"name\":\"$name\",\"baseUrl\":\"$SIMULATOR_URL\",\"username\":\"$VENDOR_USERNAME\",\"password\":\"$VENDOR_PASSWORD\"}" |
      jq -r '.id')"
  fi
  printf '%s' "$vendor_id"
}

onboard() {
  local vendor_id="$1" external_id="$2"
  curl -fsS -u "$INFRA_AUTH" -X POST "$INFRA_URL/api/vendors/$vendor_id/appliances" \
    -H 'Content-Type: application/json' \
    -d "{\"externalApplianceId\":\"$external_id\"}"
}

acme_sim="$(create_appliance acme 'E2E Acme Refrigerator' REFRIGERATOR)"
globex_sim="$(create_appliance globex 'E2E Globex Washer' WASHER)"
acme_external="$(jq -r '.id' <<<"$acme_sim")"
globex_external="$(jq -r '.id' <<<"$globex_sim")"

acme_vendor="$(ensure_vendor acme 'Acme Smart Home')"
globex_vendor="$(ensure_vendor globex 'Globex Connected Living')"
acme_infra="$(onboard "$acme_vendor" "$acme_external")"
globex_infra="$(onboard "$globex_vendor" "$globex_external")"
acme_appliance="$(jq -r '.id' <<<"$acme_infra")"
globex_appliance="$(jq -r '.id' <<<"$globex_infra")"

acme_event="$(curl -fsS -X POST "$SIMULATOR_URL/api/simulator/appliances/$acme_external/emit")"
globex_event="$(curl -fsS -X POST "$SIMULATOR_URL/api/simulator/appliances/$globex_external/emit")"

for attempt in {1..20}; do
  acme_history="$(curl -fsS -u "$INFRA_AUTH" "$INFRA_URL/api/appliances/$acme_appliance/metrics")"
  globex_history="$(curl -fsS -u "$INFRA_AUTH" "$INFRA_URL/api/appliances/$globex_appliance/metrics")"
  if [[ "$(jq 'length' <<<"$acme_history")" -ge 2 && "$(jq 'length' <<<"$globex_history")" -ge 2 ]]; then
    break
  fi
  sleep 1
done

[[ "$(jq 'length' <<<"$acme_history")" -eq 2 ]] || { echo "Expected exactly 2 Acme metric rows"; exit 1; }
[[ "$(jq 'length' <<<"$globex_history")" -eq 2 ]] || { echo "Expected exactly 2 Globex metric rows"; exit 1; }

from="$(python3 -c 'from datetime import datetime,timezone,timedelta; print((datetime.now(timezone.utc)-timedelta(minutes=5)).isoformat().replace("+00:00","Z"))')"
to="$(python3 -c 'from datetime import datetime,timezone,timedelta; print((datetime.now(timezone.utc)+timedelta(minutes=5)).isoformat().replace("+00:00","Z"))')"
report="$(curl -fsS -u "$INFRA_AUTH" -X POST "$INFRA_URL/api/reports/custom" \
  -H 'Content-Type: application/json' -d "{\"from\":\"$from\",\"to\":\"$to\"}")"

jq -n \
  --arg acmeEvent "$(jq -r '.eventId' <<<"$acme_event")" \
  --arg globexEvent "$(jq -r '.eventId' <<<"$globex_event")" \
  --argjson acmeMetrics "$acme_history" \
  --argjson globexMetrics "$globex_history" \
  --argjson report "$report" \
  '{status:"PASS", emittedEvents:{acme:$acmeEvent,globex:$globexEvent}, persistedMetrics:{acme:$acmeMetrics,globex:$globexMetrics}, customReport:$report}'
