package com.connectedhome.appliances.dto;

import com.connectedhome.appliances.model.EmissionMode;
import jakarta.validation.constraints.NotNull;

public record UpdateEmissionModeRequest(@NotNull EmissionMode emissionMode) {
}
