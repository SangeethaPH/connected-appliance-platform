package com.connectedhome.appliances.dto;

import com.connectedhome.appliances.model.ApplianceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull ApplianceStatus status) {
}
