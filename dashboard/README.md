# Connected Appliance Dashboard

Single React + TypeScript dashboard for the vendor simulator and appliance infrastructure manager.

## Capabilities

- Create simulated vendors using Acme- or Globex-compatible metric profiles
- Use the inline profile guide to compare raw metric names, units, and conversion behavior
- Create simulated appliances and manually emit Kafka events
- Register vendor connections in the infrastructure manager
- Verify and onboard simulator appliances
- View persistent appliance inventory
- Poll live normalized metrics every three seconds
- Browse historical metrics and event IDs
- Generate and review daily and custom-range reports
- Download any generated report as formatted JSON
- Reset simulator data or clear all persisted infrastructure demo data with confirmation

## Run

Start Kafka, PostgreSQL, the infrastructure manager on `8080`, and the simulator on `8081`.
Then:

```bash
npm install
cp .env.example .env
npm run dev
```

Open `http://localhost:5173`.

Set `VITE_INFRA_USERNAME` and `VITE_INFRA_PASSWORD` for infrastructure-manager Basic Auth. Keep the
values in `.env` when the backend credentials differ.

## Recommended demo flow

1. In **Vendor Simulator**, create a vendor profile and an appliance.
2. In **Infrastructure**, register the same vendor code and onboard the appliance using its simulator ID.
3. Return to **Vendor Simulator** and emit one event.
4. In **Infrastructure**, select the onboarded appliance and watch its normalized live metrics appear.
5. In **Reports**, generate a daily or custom-range report.
