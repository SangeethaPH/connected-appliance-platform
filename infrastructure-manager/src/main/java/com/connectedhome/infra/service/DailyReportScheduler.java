package com.connectedhome.infra.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.reporting.scheduling-enabled", havingValue = "true", matchIfMissing = true)
public class DailyReportScheduler {
    private final DailyReportService reportService;
    private final ZoneId reportingZone;

    public DailyReportScheduler(DailyReportService reportService,
                                @Value("${app.reporting.zone:Asia/Kolkata}") String reportingZone) {
        this.reportService = reportService;
        this.reportingZone = ZoneId.of(reportingZone);
    }

    @Scheduled(cron = "${app.reporting.daily-cron:0 5 0 * * *}", zone = "${app.reporting.zone:Asia/Kolkata}")
    public void generatePreviousDay() {
        reportService.generate(LocalDate.now(reportingZone).minusDays(1));
    }
}
