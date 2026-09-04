package com.connectedhome.appliances.dto;

import com.connectedhome.appliances.model.ApplianceType;
import com.connectedhome.appliances.model.EmissionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateApplianceRequest(
        @NotBlank String vendorId,
        @NotBlank String name,
        @NotNull ApplianceType type,
        @Min(1) long metricIntervalSeconds,
        EmissionMode emissionMode) {

    public CreateApplianceRequest {
        if (emissionMode == null) {
            emissionMode = EmissionMode.AUTOMATIC;
        }
    }
}
