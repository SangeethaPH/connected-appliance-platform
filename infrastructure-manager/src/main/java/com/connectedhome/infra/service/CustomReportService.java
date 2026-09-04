package com.connectedhome.infra.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.ApplianceDailySummary;
import com.connectedhome.infra.dto.CustomReportRequest;
import com.connectedhome.infra.dto.CustomReportResponse;
import com.connectedhome.infra.entity.CustomReportEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.repository.CustomReportRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomReportService {
    private static final Duration MAXIMUM_RANGE = Duration.ofDays(366);

    private final MetricRepository metricRepository;
    private final CustomReportRepository reportRepository;
    private final ReportAggregationService aggregationService;
    private final ObjectMapper objectMapper;
    private final Counter generatedCounter;

    public CustomReportService(MetricRepository metricRepository, CustomReportRepository reportRepository,
                               ReportAggregationService aggregationService, ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.metricRepository = metricRepository;
        this.reportRepository = reportRepository;
        this.aggregationService = aggregationService;
        this.objectMapper = objectMapper;
        this.generatedCounter = Counter.builder("infra.reports.custom.generated")
                .description("Persisted on-demand range reports").register(meterRegistry);
    }

    @Transactional
    public CustomReportResponse generate(CustomReportRequest request) {
        validateRange(request.from(), request.to());
        List<MetricEntity> samples = metricRepository
                .findAllByCapturedAtGreaterThanEqualAndCapturedAtLessThanOrderByCapturedAtAsc(
                        request.from(), request.to());
        List<ApplianceDailySummary> summaries = aggregationService.summarize(samples);
        CustomReportEntity saved = reportRepository.save(new CustomReportEntity(UUID.randomUUID(), request.from(),
                request.to(), Instant.now(), samples.size(), serialize(summaries)));
        generatedCounter.increment();
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomReportResponse find(UUID id) {
        return reportRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Custom report not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CustomReportResponse> findAll() {
        return reportRepository.findAllByOrderByGeneratedAtDesc().stream().map(this::toResponse).toList();
    }

    private void validateRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to are required");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        if (Duration.between(from, to).compareTo(MAXIMUM_RANGE) > 0) {
            throw new IllegalArgumentException("Report range cannot exceed 366 days");
        }
    }

    private String serialize(List<ApplianceDailySummary> summaries) {
        try {
            return objectMapper.writeValueAsString(summaries);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize custom report", exception);
        }
    }

    private CustomReportResponse toResponse(CustomReportEntity entity) {
        try {
            List<ApplianceDailySummary> summaries = objectMapper.readValue(
                    entity.getReportContent(), new TypeReference<>() { });
            return new CustomReportResponse(entity.getId(), entity.getPeriodStart(), entity.getPeriodEnd(),
                    entity.getGeneratedAt(), entity.getTotalSamples(), summaries);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize custom report", exception);
        }
    }
}
