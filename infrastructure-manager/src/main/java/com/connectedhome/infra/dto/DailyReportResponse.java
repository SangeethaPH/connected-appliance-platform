package com.connectedhome.infra.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailyReportResponse(
        UUID id,
        LocalDate reportDate,
        String zoneId,
        Instant periodStart,
        Instant periodEnd,
        Instant generatedAt,
        long totalSamples,
        List<ApplianceDailySummary> appliances) {
}
