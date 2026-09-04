package com.connectedhome.infra.service;

import java.util.HashMap;
import java.util.Map;

import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.messaging.RawMetricEvent;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.VendorRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class IngressRoutingService {
    private final VendorRepository vendorRepository;
    private final ApplianceRepository applianceRepository;
    private final KafkaTemplate<String, RawMetricEvent> kafkaTemplate;
    private final String validatedTopic;
    private final String quarantineTopic;
    private final Counter acceptedCounter;
    private final Counter quarantinedCounter;

    public IngressRoutingService(VendorRepository vendorRepository, ApplianceRepository applianceRepository,
                                 KafkaTemplate<String, RawMetricEvent> kafkaTemplate,
                                 @Value("${app.kafka.topics.validated-metrics}") String validatedTopic,
                                 @Value("${app.kafka.topics.quarantine}") String quarantineTopic,
                                 MeterRegistry meterRegistry) {
        this.vendorRepository = vendorRepository;
        this.applianceRepository = applianceRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.validatedTopic = validatedTopic;
        this.quarantineTopic = quarantineTopic;
        this.acceptedCounter = Counter.builder("infra.ingress.events.accepted").register(meterRegistry);
        this.quarantinedCounter = Counter.builder("infra.ingress.events.quarantined").register(meterRegistry);
    }

    public void route(RawMetricEvent event) {
        String reason = rejectionReason(event);
        String key = event == null ? "invalid" : event.vendorCode() + ":" + event.externalApplianceId();
        if (reason == null) {
            kafkaTemplate.send(validatedTopic, key, event).join();
            acceptedCounter.increment();
        } else {
            RawMetricEvent quarantined = withReason(event, reason);
            kafkaTemplate.send(quarantineTopic, key, quarantined).join();
            quarantinedCounter.increment();
        }
    }

    private String rejectionReason(RawMetricEvent event) {
        if (event == null || event.eventId() == null || event.vendorCode() == null
                || event.externalApplianceId() == null || event.capturedAt() == null
                || event.metrics() == null || event.metrics().isEmpty()) return "INVALID_ENVELOPE";
        if (!"1.0".equals(event.schemaVersion())) return "UNSUPPORTED_SCHEMA";
        VendorEntity vendor = vendorRepository.findByCodeIgnoreCase(event.vendorCode()).orElse(null);
        if (vendor == null) return "VENDOR_NOT_REGISTERED";
        if (applianceRepository.findByVendorIdAndExternalId(vendor.getId(), event.externalApplianceId()).isEmpty())
            return "APPLIANCE_NOT_ONBOARDED";
        return null;
    }

    private RawMetricEvent withReason(RawMetricEvent event, String reason) {
        if (event == null) return null;
        Map<String, String> metadata = new HashMap<>(event.metadata() == null ? Map.of() : event.metadata());
        metadata.put("quarantineReason", reason);
        return new RawMetricEvent(event.eventId(), event.vendorCode(), event.externalApplianceId(),
                event.applianceType(), event.schemaVersion(), event.capturedAt(), event.metrics(), Map.copyOf(metadata));
    }
}
