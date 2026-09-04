package com.connectedhome.infra.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomReportResponse(
        UUID id,
        Instant from,
        Instant to,
        Instant generatedAt,
        long totalSamples,
        List<ApplianceDailySummary> appliances) {
}
