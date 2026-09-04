package com.connectedhome.infra.dto;

import java.time.Instant;
import java.util.UUID;

import com.connectedhome.infra.model.ApplianceStatus;

public record ApplianceResponse(UUID id, UUID vendorId, String externalId, String name, String type,
                                ApplianceStatus status, Instant onboardedAt, Instant lastSeenAt) {
}
