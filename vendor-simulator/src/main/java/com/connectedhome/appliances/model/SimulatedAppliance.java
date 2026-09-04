package com.connectedhome.appliances.model;

import java.time.Instant;

public record SimulatedAppliance(
        String id,
        String vendorId,
        String name,
        ApplianceType type,
        ApplianceStatus status,
        EmissionMode emissionMode,
        long metricIntervalSeconds,
        Instant lastMetricAt) {

    public SimulatedAppliance withStatus(ApplianceStatus newStatus) {
        return new SimulatedAppliance(id, vendorId, name, type, newStatus, emissionMode,
                metricIntervalSeconds, lastMetricAt);
    }

    public SimulatedAppliance withEmissionMode(EmissionMode newMode) {
        return new SimulatedAppliance(id, vendorId, name, type, status, newMode,
                metricIntervalSeconds, lastMetricAt);
    }

    public SimulatedAppliance emittedAt(Instant timestamp) {
        return new SimulatedAppliance(id, vendorId, name, type, status, emissionMode,
                metricIntervalSeconds, timestamp);
    }
}
