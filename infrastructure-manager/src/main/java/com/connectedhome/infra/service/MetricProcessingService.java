package com.connectedhome.infra.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.adapter.MetricAdapterRegistry;
import com.connectedhome.infra.adapter.NormalizedMetric;
import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.exception.UnsupportedVendorMetricException;
import com.connectedhome.infra.messaging.RawMetricEvent;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.connectedhome.infra.repository.VendorRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricProcessingService {
    private final VendorRepository vendorRepository;
    private final ApplianceRepository applianceRepository;
    private final MetricRepository metricRepository;
    private final MetricAdapterRegistry adapterRegistry;
    private final Counter processedCounter;
    private final Counter duplicateCounter;
    private final Counter failedCounter;

    public MetricProcessingService(VendorRepository vendorRepository, ApplianceRepository applianceRepository,
                                   MetricRepository metricRepository, MetricAdapterRegistry adapterRegistry,
                                   MeterRegistry meterRegistry) {
        this.vendorRepository = vendorRepository;
        this.applianceRepository = applianceRepository;
        this.metricRepository = metricRepository;
        this.adapterRegistry = adapterRegistry;
        this.processedCounter = Counter.builder("infra.kafka.events.processed")
                .description("Raw events normalized and persisted").register(meterRegistry);
        this.duplicateCounter = Counter.builder("infra.kafka.events.duplicate")
                .description("Raw events skipped by event ID").register(meterRegistry);
        this.failedCounter = Counter.builder("infra.kafka.events.failed")
                .description("Raw events rejected during processing").register(meterRegistry);
    }

    @Transactional
    public void process(RawMetricEvent event) {
        try {
            validateEnvelope(event);
            if (metricRepository.existsByEventId(event.eventId())) {
                duplicateCounter.increment();
                return;
            }

            VendorEntity vendor = vendorRepository.findByCodeIgnoreCase(event.vendorCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + event.vendorCode()));
            ApplianceEntity appliance = applianceRepository
                    .findByVendorIdAndExternalId(vendor.getId(), event.externalApplianceId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Onboarded appliance not found: " + event.externalApplianceId()));

            String metricProfile = event.metadata() == null ? null : event.metadata().get("metricProfile");
            List<NormalizedMetric> normalized = adapterRegistry.require(event.vendorCode(), metricProfile)
                    .normalize(event);
            if (normalized.isEmpty()) {
                throw new UnsupportedVendorMetricException("Event contains no supported metrics: " + event.eventId());
            }

            Instant receivedAt = Instant.now();
            List<MetricEntity> entities = normalized.stream()
                    .map(metric -> new MetricEntity(UUID.randomUUID(), event.eventId(), appliance.getId(),
                            metric.name(), metric.value(), metric.unit(), event.capturedAt(), receivedAt))
                    .toList();
            metricRepository.saveAll(entities);
            processedCounter.increment();
        } catch (RuntimeException exception) {
            failedCounter.increment();
            throw exception;
        }
    }

    private void validateEnvelope(RawMetricEvent event) {
        if (event == null || event.eventId() == null || event.vendorCode() == null
                || event.externalApplianceId() == null || event.capturedAt() == null
                || event.metrics() == null || event.metrics().isEmpty()) {
            throw new IllegalArgumentException("Invalid raw metric event envelope");
        }
        if (!"1.0".equals(event.schemaVersion())) {
            throw new IllegalArgumentException("Unsupported metric schema version: " + event.schemaVersion());
        }
    }
}
