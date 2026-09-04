package com.connectedhome.appliances.model;

import java.time.Instant;

public record ApplianceMetric(
        String applianceId,
        String vendorId,
        String metricName,
        double value,
        String unit,
        Instant capturedAt) {
}
