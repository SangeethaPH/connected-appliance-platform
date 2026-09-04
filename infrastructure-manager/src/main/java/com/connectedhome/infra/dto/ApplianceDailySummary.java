package com.connectedhome.infra.dto;

import java.util.List;
import java.util.UUID;

public record ApplianceDailySummary(
        UUID applianceId,
        String externalApplianceId,
        String applianceName,
        String applianceType,
        UUID vendorId,
        String vendorCode,
        String vendorName,
        List<MetricAggregate> metrics) {
}
