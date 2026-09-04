package com.connectedhome.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.DailyReportResponse;
import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.model.ApplianceStatus;
import com.connectedhome.infra.model.AuthenticationType;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.DailyReportRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.connectedhome.infra.repository.VendorRepository;
import com.connectedhome.infra.security.CredentialCipher;
import com.connectedhome.infra.service.DailyReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DailyReportServiceIntegrationTest {
    @Autowired DailyReportService reportService;
    @Autowired DailyReportRepository reportRepository;
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
    void generatesAndPersistsDailyAggregates() {
        LocalDate date = LocalDate.of(2026, 8, 23);
        Instant capturedAt = date.atTime(12, 0).atZone(ZoneId.of("Asia/Kolkata")).toInstant();
        VendorEntity vendor = vendorRepository.save(new VendorEntity(UUID.randomUUID(), "acme", "Acme",
                "http://localhost:8081", AuthenticationType.BASIC, "user",
                credentialCipher.encrypt("password"), Instant.now()));
        ApplianceEntity appliance = applianceRepository.save(new ApplianceEntity(UUID.randomUUID(), vendor.getId(),
                "fridge-1", "Kitchen Fridge", "REFRIGERATOR", ApplianceStatus.ONLINE,
                Instant.now(), Instant.now()));
        UUID eventOne = UUID.randomUUID();
        UUID eventTwo = UUID.randomUUID();
        metricRepository.saveAll(List.of(
                new MetricEntity(UUID.randomUUID(), eventOne, appliance.getId(), "POWER", 100, "W",
                        capturedAt, Instant.now()),
                new MetricEntity(UUID.randomUUID(), eventTwo, appliance.getId(), "POWER", 300, "W",
                        capturedAt.plusSeconds(60), Instant.now())));

        DailyReportResponse report = reportService.generate(date);

        assertThat(report.totalSamples()).isEqualTo(2);
        assertThat(report.appliances()).hasSize(1);
        assertThat(report.appliances().getFirst().metrics().getFirst().average()).isEqualTo(200.0);
        assertThat(reportRepository.count()).isEqualTo(1);

        reportService.generate(date);
        assertThat(reportRepository.count()).isEqualTo(1);
    }
}
