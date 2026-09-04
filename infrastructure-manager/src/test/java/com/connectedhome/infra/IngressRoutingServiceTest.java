package com.connectedhome.infra;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.messaging.RawMetricEvent;
import com.connectedhome.infra.model.ApplianceStatus;
import com.connectedhome.infra.model.AuthenticationType;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.VendorRepository;
import com.connectedhome.infra.service.IngressRoutingService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class IngressRoutingServiceTest {
    private VendorRepository vendorRepository;
    private ApplianceRepository applianceRepository;
    private KafkaTemplate<String, RawMetricEvent> kafkaTemplate;
    private IngressRoutingService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        vendorRepository = mock(VendorRepository.class);
        applianceRepository = mock(ApplianceRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(RawMetricEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        service = new IngressRoutingService(vendorRepository, applianceRepository, kafkaTemplate,
                "validated", "quarantine", new SimpleMeterRegistry());
    }

    @Test
    void routesOnboardedApplianceToValidatedTopic() {
        RawMetricEvent event = event("acme", "fan-1");
        VendorEntity vendor = vendor();
        when(vendorRepository.findByCodeIgnoreCase("acme")).thenReturn(Optional.of(vendor));
        when(applianceRepository.findByVendorIdAndExternalId(vendor.getId(), "fan-1"))
                .thenReturn(Optional.of(appliance(vendor)));

        service.route(event);

        verify(kafkaTemplate).send(eq("validated"), eq("acme:fan-1"), eq(event));
    }

    @Test
    void routesUnknownVendorToQuarantineTopic() {
        RawMetricEvent event = event("unknown", "fan-1");
        when(vendorRepository.findByCodeIgnoreCase("unknown")).thenReturn(Optional.empty());

        service.route(event);

        verify(kafkaTemplate).send(eq("quarantine"), eq("unknown:fan-1"), any(RawMetricEvent.class));
    }

    private RawMetricEvent event(String vendor, String appliance) {
        return new RawMetricEvent(UUID.randomUUID(), vendor, appliance, "FAN", "1.0", Instant.now(),
                Map.of("power_watts", 80.0, "fan_speed_pct", 60.0), Map.of("metricProfile", "ACME"));
    }

    private VendorEntity vendor() {
        return new VendorEntity(UUID.randomUUID(), "acme", "Acme", "http://localhost:8081",
                AuthenticationType.BASIC, "user", "encrypted", Instant.now());
    }

    private ApplianceEntity appliance(VendorEntity vendor) {
        return new ApplianceEntity(UUID.randomUUID(), vendor.getId(), "fan-1", "Fan", "FAN",
                ApplianceStatus.ONLINE, Instant.now(), Instant.now());
    }
}
