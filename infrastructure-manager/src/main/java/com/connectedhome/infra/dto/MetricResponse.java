package com.connectedhome.infra.dto;

import java.time.Instant;
import java.util.UUID;

public record MetricResponse(
        UUID id,
        UUID eventId,
        UUID applianceId,
        String metricName,
        double value,
        String unit,
        Instant capturedAt,
        Instant receivedAt) {
}
