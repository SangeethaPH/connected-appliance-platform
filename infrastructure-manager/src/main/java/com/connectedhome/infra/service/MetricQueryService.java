package com.connectedhome.infra.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.MetricResponse;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.MetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricQueryService {
    private final ApplianceRepository applianceRepository;
    private final MetricRepository metricRepository;

    public MetricQueryService(ApplianceRepository applianceRepository, MetricRepository metricRepository) {
        this.applianceRepository = applianceRepository;
        this.metricRepository = metricRepository;
    }

    @Transactional(readOnly = true)
    public List<MetricResponse> history(UUID applianceId, Instant from, Instant to) {
        if (!applianceRepository.existsById(applianceId)) {
            throw new ResourceNotFoundException("Appliance not found: " + applianceId);
        }
        Instant effectiveTo = to == null ? Instant.now() : to;
        Instant effectiveFrom = from == null ? effectiveTo.minus(24, ChronoUnit.HOURS) : from;
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        return metricRepository.findAllByApplianceIdAndCapturedAtBetweenOrderByCapturedAtAsc(
                        applianceId, effectiveFrom, effectiveTo).stream()
                .map(metric -> new MetricResponse(metric.getId(), metric.getEventId(), metric.getApplianceId(),
                        metric.getMetricName(), metric.getValue(), metric.getUnit(), metric.getCapturedAt(),
                        metric.getReceivedAt()))
                .toList();
    }
}
