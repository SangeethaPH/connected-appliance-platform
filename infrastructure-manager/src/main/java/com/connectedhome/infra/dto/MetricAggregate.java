package com.connectedhome.infra.dto;

public record MetricAggregate(
        String metricName,
        String unit,
        long sampleCount,
        double minimum,
        double maximum,
        double average) {
}
