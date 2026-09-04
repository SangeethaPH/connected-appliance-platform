package com.connectedhome.appliances.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RawVendorMetric(
        UUID eventId,
        String vendorCode,
        String externalApplianceId,
        String applianceType,
        String schemaVersion,
        Instant capturedAt,
        Map<String, Double> metrics,
        Map<String, String> metadata) {
}
