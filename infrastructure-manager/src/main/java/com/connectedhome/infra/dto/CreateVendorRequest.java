package com.connectedhome.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateVendorRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]+") String code,
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String username,
        @NotBlank String password) {
}
