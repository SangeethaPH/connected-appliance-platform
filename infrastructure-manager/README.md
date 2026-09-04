# Appliance Infrastructure Manager

Connected-appliance management backend with persistent onboarding and Kafka metric ingestion.

## Architecture in this increment

```text
Vendor appliances --> Kafka raw topic --> Ingress validation
                                            |          |
                                            |          +--> Quarantine (unknown/unregistered)
                                            v
                                      Validated topic --> Metric processor --> PostgreSQL
                                                                  |
                                                                  +--> DLT on processing failure
```

The backend runs on port `8080`; the existing vendor simulator runs on `8081`.

## Implemented

- Secured management API using HTTP Basic Auth
- Persistent vendor records in PostgreSQL
- AES-256-GCM encryption for stored vendor passwords
- Vendor connector abstraction with a Basic Auth implementation
- Appliance onboarding that verifies the appliance against the vendor before saving
- Persistent onboarded-appliance inventory
- Kafka consumer group for raw vendor events
- Two-stage Kafka ingress with separate validator and metric-processor consumer groups
- Quarantine routing for unknown vendors, appliances, schemas, and malformed envelopes
- Shared raw topic partitioned by vendor/appliance key
- Acme and Globex normalization adapters
- Idempotent metric storage using event IDs
- Historical metric queries with optional date ranges
- Two retries followed by dead-letter publishing
- Scheduled and manually triggered daily reports
- Persistent daily count/minimum/maximum/average aggregates
- Persistent on-demand reports for arbitrary ISO-8601 ranges up to 366 days
- Prometheus counters for Kafka processing, duplicates, failures, and report generation
- OpenAPI JSON and Swagger UI for end-to-end API review
- Flyway schema migrations, validation, API errors and integration tests

## Requirements

- Java 21
- Maven 3.6.3+
- PostgreSQL 17 running locally
- Apache Kafka running locally on port `9092`
- The vendor simulator from the previous step running on port `8081`

## Local Kafka setup

```bash
brew install kafka
brew services start kafka
kafka-topics --bootstrap-server localhost:9092 --list
```

The backend creates these three-partition topics automatically:

```text
appliance.metrics.raw.v1
appliance.metrics.validated.v1
appliance.metrics.quarantine.v1
appliance.metrics.dlt.v1
```

## Local PostgreSQL setup

Start PostgreSQL installed through Homebrew:

```bash
brew services start postgresql@17
```

The local profile expects:

```text
Host: localhost
Port: 5432
Database: appliances
Username: appliance_user
Password: set through `DB_PASSWORD`
```

Verify the connection before starting Spring Boot:

```bash
/opt/homebrew/opt/postgresql@17/bin/psql \
  -h localhost \
  -U appliance_user \
  -d appliances \
  -c "SELECT current_database(), current_user;"
```

## Run the backend

```bash
mvn clean test
mvn spring-boot:run
```

The `local` Spring profile is selected automatically. Flyway creates the `vendors`,
`appliances`, `appliance_metrics`, `daily_reports`, `custom_reports`, and
`flyway_schema_history` tables.

To override the local database settings:

```bash
DB_URL=jdbc:postgresql://localhost:5432/appliances \
DB_USERNAME=appliance_user \
DB_PASSWORD='<your-local-db-password>' \
mvn spring-boot:run
```

Default backend login for local development:

```text
username: admin
password: set through `ADMIN_PASSWORD`
```

Override it using `ADMIN_USERNAME` and `ADMIN_PASSWORD`.

## Test Step 1

First create a simulated appliance in the vendor simulator and copy its generated `id`.

Register the vendor in this backend:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -X POST http://localhost:8080/api/vendors \
  -H 'Content-Type: application/json' \
  -d '{
    "code":"acme",
    "name":"Acme Smart Home",
    "baseUrl":"http://localhost:8081",
    "username":"<vendor-username>",
    "password":"<vendor-password>"
  }'
```

Copy the returned vendor `id`, then onboard the simulator appliance:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -X POST http://localhost:8080/api/vendors/{vendorId}/appliances \
  -H 'Content-Type: application/json' \
  -d '{"externalApplianceId":"{simulatorApplianceId}"}'
```

List persistent inventory:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" http://localhost:8080/api/appliances
```

Verify authentication:

```bash
curl -i http://localhost:8080/api/vendors
```

The response should be `401 Unauthorized`.

## Inspect persisted data

```bash
/opt/homebrew/opt/postgresql@17/bin/psql \
  -h localhost -U appliance_user -d appliances
```

Inside `psql`:

```sql
\dt
SELECT id, code, name, base_url, auth_type, created_at FROM vendors;
SELECT id, vendor_id, external_id, name, type, status FROM appliances;
SELECT event_id, appliance_id, metric_name, value, unit, captured_at
FROM appliance_metrics ORDER BY captured_at DESC;
SELECT report_date, zone_id, total_samples, generated_at
FROM daily_reports ORDER BY report_date DESC;
SELECT id, period_start, period_end, total_samples, generated_at
FROM custom_reports ORDER BY generated_at DESC;
```

The included `compose.yml` is optional and is not needed for local execution.

## Verify automatic metric history

After the vendor and its appliance have been onboarded, keep Kafka and both applications running.
The simulator publishes each emission to Kafka. This backend listens with consumer group
`appliance-infrastructure-manager-v1`, validates onboarding, selects the vendor adapter,
normalizes the fields and persists them.

Copy the infrastructure manager's appliance `id` and query its last 24 hours:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" \
  http://localhost:8080/api/appliances/{infrastructureApplianceId}/metrics
```

Query an explicit ISO-8601 range:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" \
  'http://localhost:8080/api/appliances/{id}/metrics?from=2026-08-23T00:00:00Z&to=2026-08-24T00:00:00Z'
```

## Daily reports

Daily boundaries use `Asia/Kolkata` by default. At `00:05` the scheduler generates the
previous day's report. Override the timezone or cron with `REPORTING_ZONE` and
`DAILY_REPORT_CRON`.

Generate a daily report immediately for demo verification:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -X POST \
  http://localhost:8080/api/reports/daily/2026-08-23
```

Retrieve a persisted report:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" \
  http://localhost:8080/api/reports/daily/2026-08-23
```

List all generated reports:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" http://localhost:8080/api/reports/daily
```

## Custom date-range reports

Generate and persist an on-demand report. The `from` boundary is inclusive and `to` is
exclusive; the maximum range is 366 days.

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -X POST http://localhost:8080/api/reports/custom \
  -H 'Content-Type: application/json' \
  -d '{"from":"2026-08-24T10:00:00Z","to":"2026-08-24T12:00:00Z"}'
```

Use the returned report ID to retrieve it, or list all custom reports:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" http://localhost:8080/api/reports/custom/{reportId}
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" http://localhost:8080/api/reports/custom
```

## Multi-vendor end-to-end test

With Kafka, PostgreSQL, the simulator, and this backend running, execute:

```bash
./scripts/e2e-multi-vendor.sh
```

The script creates manual-only Acme and Globex appliances, onboards them, emits exactly one
event each, waits for Kafka ingestion, verifies exactly two normalized metric rows per event,
and generates a persisted custom report. Its final JSON has `"status":"PASS"` and displays
both vendor histories so the normalized units can be reviewed.

## API documentation and operational metrics

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Prometheus metrics (authenticated): `http://localhost:8080/actuator/prometheus`

Useful counters start with `infra_kafka_events_` and `infra_reports_`.

## Verify ingress validation and quarantine

Watch accepted events:

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic appliance.metrics.validated.v1 --from-beginning \
  --property print.key=true
```

Watch rejected events and their `metadata.quarantineReason`:

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic appliance.metrics.quarantine.v1 --from-beginning \
  --property print.key=true
```

Consumer groups:

```text
appliance-ingress-validator-v1  -> raw topic
appliance-metric-processor-v1   -> validated topic
```

Clear all local demo vendors, appliances, metric history and reports:

```bash
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -X DELETE http://localhost:8080/api/admin/demo-data
```

This endpoint is intended for local demo reset and remains protected by the backend's Basic Auth.

## Story status

The backend story scope is complete: vendor/appliance management, event-driven multi-vendor
ingestion, historical persistence, scheduled daily reports, custom-range reports, API review,
observability, and repeatable end-to-end verification.
