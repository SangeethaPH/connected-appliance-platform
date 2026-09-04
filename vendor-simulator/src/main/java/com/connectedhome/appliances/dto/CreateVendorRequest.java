package com.connectedhome.appliances.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateVendorRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]+") String id,
        @NotBlank String name,
        @NotBlank String apiStyle,
        @NotBlank String authentication,
        @NotBlank @Pattern(regexp = "ACME|GLOBEX") String metricProfile) {
    public CreateVendorRequest {
        id = id == null ? null : id.trim();
        name = name == null ? null : name.trim();
    }
}
