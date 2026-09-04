# Connected Appliance Vendor Simulator

Spring Boot service for the first runnable slice of the connected-appliance demo. It simulates vendors and connected appliances that emit metrics at configurable intervals.

## Included

- Two mock vendors with different advertised authentication styles
- Create, list, inspect, and enable/disable simulated appliances
- Scheduled metric emission per appliance
- Manual metric emission for deterministic demos
- Per-appliance `AUTOMATIC` or `MANUAL_ONLY` emission mode
- Vendor-specific raw metric event followed by normalization
- Kafka publishing with appliance-level ordering keys
- Different raw metric names and units for Acme and Globex
- In-memory metric history for this simulator slice
- Validation, consistent API errors, Actuator health, and integration tests
- Prometheus counters for acknowledged and failed Kafka publication

## Requirements

- Java 17 or newer
- Maven 3.6.3+

## Run

```bash
mvn clean test
mvn spring-boot:run
```

The service runs on `http://localhost:8081`. Every emitted metric event is published to
`appliance.metrics.raw.v1` using `vendorCode:externalApplianceId` as the Kafka message key.
Kafka defaults to `localhost:9092`.

## End-to-end verification

List vendors:

```bash
curl http://localhost:8081/api/vendors
```

Create a refrigerator that emits every five seconds:

```bash
curl -X POST http://localhost:8081/api/simulator/appliances \
  -H 'Content-Type: application/json' \
  -d '{"vendorId":"acme","name":"Kitchen Fridge","type":"REFRIGERATOR","metricIntervalSeconds":5,"emissionMode":"AUTOMATIC"}'
```

Copy the returned `id`, then force one emission:

```bash
curl -X POST http://localhost:8081/api/simulator/appliances/{id}/emit
```

Read normalized metrics:

```bash
curl http://localhost:8081/api/simulator/appliances/{id}/metrics
```

Switch to manual-only emission:

```bash
curl -X PATCH http://localhost:8081/api/simulator/appliances/{id}/emission-mode \
  -H 'Content-Type: application/json' \
  -d '{"emissionMode":"MANUAL_ONLY"}'
```

The manual endpoint continues to work in `MANUAL_ONLY` mode:

```bash
curl -X POST http://localhost:8081/api/simulator/appliances/{id}/emit
```

Switch automatic scheduling back on:

```bash
curl -X PATCH http://localhost:8081/api/simulator/appliances/{id}/emission-mode \
  -H 'Content-Type: application/json' \
  -d '{"emissionMode":"AUTOMATIC"}'
```

Mark an appliance offline:

```bash
curl -X PATCH http://localhost:8081/api/simulator/appliances/{id}/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"OFFLINE"}'
```

Health check:

```bash
curl http://localhost:8081/actuator/health
```

Prometheus metrics:

```bash
curl http://localhost:8081/actuator/prometheus | grep simulator_kafka_events
```

Reset all simulated appliances, in-memory metrics and custom vendors (Acme and Globex defaults
are restored):

```bash
curl -X DELETE http://localhost:8081/api/simulator/data
```

## Design boundary

This module represents the external-vendor side of the larger system. It knows only Kafka and
does not know the infrastructure manager's address. Acme emits Celsius/watts fields while
Globex emits Fahrenheit/kilowatt/ratio fields, allowing the backend adapters to demonstrate
vendor-specific normalization.
