package com.connectedhome.infra.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record CustomReportRequest(@NotNull Instant from, @NotNull Instant to) {
}
