# Connected Appliance Platform

An end-to-end demonstration of a vendor-neutral platform for onboarding connected appliances, ingesting vendor-specific metrics through Kafka, preserving historical data in PostgreSQL, and generating daily or custom-range reports.

## Applications

| Folder | Technology | Port | Responsibility |
| --- | --- | --- | --- |
| `vendor-simulator` | Java 21, Spring Boot | 8081 | Creates simulated vendors and appliances and publishes manual or automatic metric events |
| `infrastructure-manager` | Java 21, Spring Boot | 8080 | Registers vendors, onboards appliances, validates and normalizes events, persists metrics, and generates reports |
| `dashboard` | React, TypeScript, Vite | 5173 | Operates the simulator and infrastructure APIs from one review interface |

## Event flow

```text
Vendor simulator
    -> appliance.metrics.raw.v1
    -> ingress validation
       -> appliance.metrics.quarantine.v1 (unknown or invalid)
       -> appliance.metrics.validated.v1 (accepted)
          -> metric normalization and idempotent persistence
             -> PostgreSQL
                -> metric history and reports

Processing failures after admission -> appliance.metrics.dlt.v1
```

Kafka messages are keyed by `vendorCode:externalApplianceId`, preserving ordering for each appliance. ACME and GLOBEX adapters demonstrate how different vendor metric names and units are converted into a canonical model.

## Requirements

- Java 21 and Maven
- Node.js and npm
- PostgreSQL 17 on `localhost:5432`
- Kafka on `localhost:9092`

The local infrastructure profile expects database `appliances`, user `appliance_user`, and the demo password documented in the infrastructure-manager README. All local credentials are overrideable through environment variables and are not production credentials.

## Run locally

Start dependencies:

```bash
brew services start postgresql@17
brew services start kafka
```

Start the vendor simulator:

```bash
cd vendor-simulator
mvn spring-boot:run
```

Start the infrastructure manager in another terminal:

```bash
cd infrastructure-manager
mvn spring-boot:run
```

Start the dashboard:

```bash
cd dashboard
npm install
npm run dev
```

Open `http://localhost:5173`. API documentation is available at `http://localhost:8080/swagger-ui/index.html`.

## Verify

```bash
cd vendor-simulator && mvn test
cd ../infrastructure-manager && mvn test
cd ../dashboard && npm install && npm run build
```

Detailed API, PostgreSQL, Kafka, reporting, reset, and multi-vendor verification commands are available in the component READMEs.
