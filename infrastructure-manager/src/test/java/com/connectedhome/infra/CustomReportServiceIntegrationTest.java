package com.connectedhome.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.CustomReportRequest;
import com.connectedhome.infra.dto.CustomReportResponse;
import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.model.ApplianceStatus;
import com.connectedhome.infra.model.AuthenticationType;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.CustomReportRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.connectedhome.infra.repository.VendorRepository;
import com.connectedhome.infra.security.CredentialCipher;
import com.connectedhome.infra.service.CustomReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomReportServiceIntegrationTest {
    @Autowired CustomReportService reportService;
    @Autowired CustomReportRepository reportRepository;
    @Autowired MetricRepository metricRepository;
    @Autowired ApplianceRepository applianceRepository;
    @Autowired VendorRepository vendorRepository;
    @Autowired CredentialCipher credentialCipher;

    @BeforeEach
    void cleanDatabase() {
        reportRepository.deleteAll();
        metricRepository.deleteAll();
        applianceRepository.deleteAll();
        vendorRepository.deleteAll();
    }

    @Test
    void generatesPersistsAndRetrievesRangeReport() {
        Instant from = Instant.parse("2026-08-24T00:00:00Z");
        VendorEntity vendor = vendorRepository.save(new VendorEntity(UUID.randomUUID(), "globex", "Globex",
                "http://localhost:8081", AuthenticationType.BASIC, "user",
                credentialCipher.encrypt("password"), Instant.now()));
        ApplianceEntity appliance = applianceRepository.save(new ApplianceEntity(UUID.randomUUID(), vendor.getId(),
                "washer-1", "Laundry Washer", "WASHER", ApplianceStatus.ONLINE, Instant.now(), Instant.now()));
        metricRepository.saveAll(List.of(
                new MetricEntity(UUID.randomUUID(), UUID.randomUUID(), appliance.getId(), "POWER", 500, "W",
                        from.plusSeconds(60), Instant.now()),
                new MetricEntity(UUID.randomUUID(), UUID.randomUUID(), appliance.getId(), "POWER", 700, "W",
                        from.plusSeconds(120), Instant.now())));

        CustomReportResponse generated = reportService.generate(
                new CustomReportRequest(from, from.plusSeconds(3600)));

        assertThat(generated.totalSamples()).isEqualTo(2);
        assertThat(generated.appliances()).hasSize(1);
        assertThat(generated.appliances().getFirst().metrics().getFirst().average()).isEqualTo(600.0);
        assertThat(reportService.find(generated.id())).isEqualTo(generated);
        assertThat(reportRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidAndExcessiveRanges() {
        Instant from = Instant.parse("2026-08-24T00:00:00Z");
        assertThatThrownBy(() -> reportService.generate(new CustomReportRequest(from, from)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("from must be before to");
        assertThatThrownBy(() -> reportService.generate(
                new CustomReportRequest(from, from.plusSeconds(367L * 24 * 60 * 60))))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Report range cannot exceed 366 days");
    }
}
