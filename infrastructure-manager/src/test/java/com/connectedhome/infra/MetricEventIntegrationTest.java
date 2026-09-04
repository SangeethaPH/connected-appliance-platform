package com.connectedhome.infra;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.model.ApplianceStatus;
import com.connectedhome.infra.model.AuthenticationType;
import com.connectedhome.infra.messaging.RawMetricEvent;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.connectedhome.infra.repository.VendorRepository;
import com.connectedhome.infra.security.CredentialCipher;
import com.connectedhome.infra.service.MetricProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class MetricEventIntegrationTest {
    @Autowired VendorRepository vendorRepository;
    @Autowired ApplianceRepository applianceRepository;
    @Autowired MetricRepository metricRepository;
    @Autowired CredentialCipher credentialCipher;
    @Autowired MetricProcessingService processingService;

    @BeforeEach
    void cleanDatabase() {
        metricRepository.deleteAll();
        applianceRepository.deleteAll();
        vendorRepository.deleteAll();
    }

    @Test
    void normalizesAndPersistsMetricsForOnboardedAppliance() {
        Instant now = Instant.now();
        VendorEntity vendor = vendorRepository.save(new VendorEntity(UUID.randomUUID(), "acme", "Acme",
                "http://localhost:8081", AuthenticationType.BASIC, "test-vendor-user",
                credentialCipher.encrypt("test-only-vendor-password"), now));
        ApplianceEntity appliance = applianceRepository.save(new ApplianceEntity(UUID.randomUUID(), vendor.getId(),
                "external-fridge-1", "Kitchen Fridge", "REFRIGERATOR", ApplianceStatus.ONLINE, now, now));
        UUID eventId = UUID.randomUUID();

        processingService.process(new RawMetricEvent(eventId, "acme", "external-fridge-1",
                "REFRIGERATOR", "1.0", now,
                Map.of("power_watts", 120.5, "internal_temp_c", 4.2), Map.of()));

        assertThat(metricRepository.count()).isEqualTo(2);
        assertThat(metricRepository.findAll())
                .allMatch(metric -> metric.getApplianceId().equals(appliance.getId()))
                .allMatch(metric -> metric.getEventId().equals(eventId));
    }
}
