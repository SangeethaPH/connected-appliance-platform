package com.connectedhome.infra.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.ApplianceDailySummary;
import com.connectedhome.infra.dto.DailyReportResponse;
import com.connectedhome.infra.entity.DailyReportEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.repository.DailyReportRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {
    private final MetricRepository metricRepository;
    private final DailyReportRepository reportRepository;
    private final ReportAggregationService aggregationService;
    private final ObjectMapper objectMapper;
    private final ZoneId reportingZone;
    private final Counter generatedCounter;

    public DailyReportService(MetricRepository metricRepository, DailyReportRepository reportRepository,
                              ReportAggregationService aggregationService, ObjectMapper objectMapper,
                              @Value("${app.reporting.zone:Asia/Kolkata}") String reportingZone,
                              MeterRegistry meterRegistry) {
        this.metricRepository = metricRepository;
        this.reportRepository = reportRepository;
        this.aggregationService = aggregationService;
        this.objectMapper = objectMapper;
        this.reportingZone = ZoneId.of(reportingZone);
        this.generatedCounter = Counter.builder("infra.reports.daily.generated")
                .description("Persisted daily reports").register(meterRegistry);
    }

    @Transactional
    public DailyReportResponse generate(LocalDate reportDate) {
        reportRepository.findByReportDate(reportDate).ifPresent(reportRepository::delete);
        reportRepository.flush();
        return generateNew(reportDate);
    }

    @Transactional(readOnly = true)
    public DailyReportResponse find(LocalDate reportDate) {
        return reportRepository.findByReportDate(reportDate)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Daily report not found: " + reportDate));
    }

    @Transactional(readOnly = true)
    public List<DailyReportResponse> findAll() {
        return reportRepository.findAllByOrderByReportDateDesc().stream().map(this::toResponse).toList();
    }

    private DailyReportResponse generateNew(LocalDate reportDate) {
        Instant from = reportDate.atStartOfDay(reportingZone).toInstant();
        Instant to = reportDate.plusDays(1).atStartOfDay(reportingZone).toInstant();
        List<MetricEntity> samples = metricRepository
                .findAllByCapturedAtGreaterThanEqualAndCapturedAtLessThanOrderByCapturedAtAsc(from, to);

        List<ApplianceDailySummary> summaries = aggregationService.summarize(samples);
        Instant generatedAt = Instant.now();
        DailyReportEntity saved = reportRepository.save(new DailyReportEntity(
                UUID.randomUUID(), reportDate, reportingZone.getId(), from, to, generatedAt,
                samples.size(), serialize(summaries)));
        generatedCounter.increment();
        return toResponse(saved);
    }

    private String serialize(List<ApplianceDailySummary> summaries) {
        try {
            return objectMapper.writeValueAsString(summaries);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize daily report", exception);
        }
    }

    private DailyReportResponse toResponse(DailyReportEntity entity) {
        try {
            List<ApplianceDailySummary> summaries = objectMapper.readValue(
                    entity.getReportContent(), new TypeReference<>() { });
            return new DailyReportResponse(entity.getId(), entity.getReportDate(), entity.getZoneId(),
                    entity.getPeriodStart(), entity.getPeriodEnd(), entity.getGeneratedAt(),
                    entity.getTotalSamples(), summaries);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize daily report", exception);
        }
    }
}
