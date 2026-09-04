package com.connectedhome.infra.dto;

import java.time.Instant;
import java.util.UUID;

import com.connectedhome.infra.model.AuthenticationType;

public record VendorResponse(UUID id, String code, String name, String baseUrl,
                             AuthenticationType authenticationType, String username, Instant createdAt) {
}
