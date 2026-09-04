package com.connectedhome.infra.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardApplianceRequest(@NotBlank String externalApplianceId) {
}
