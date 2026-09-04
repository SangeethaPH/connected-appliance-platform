package com.connectedhome.infra.service;

import java.util.Map;

import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.CustomReportRepository;
import com.connectedhome.infra.repository.DailyReportRepository;
import com.connectedhome.infra.repository.MetricRepository;
import com.connectedhome.infra.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataResetService {
    private final CustomReportRepository customReportRepository;
    private final DailyReportRepository dailyReportRepository;
    private final MetricRepository metricRepository;
    private final ApplianceRepository applianceRepository;
    private final VendorRepository vendorRepository;

    public DemoDataResetService(CustomReportRepository customReportRepository,
                                DailyReportRepository dailyReportRepository,
                                MetricRepository metricRepository,
                                ApplianceRepository applianceRepository,
                                VendorRepository vendorRepository) {
        this.customReportRepository = customReportRepository;
        this.dailyReportRepository = dailyReportRepository;
        this.metricRepository = metricRepository;
        this.applianceRepository = applianceRepository;
        this.vendorRepository = vendorRepository;
    }

    @Transactional
    public Map<String, Object> clearAll() {
        long customReports = customReportRepository.count();
        long dailyReports = dailyReportRepository.count();
        long metrics = metricRepository.count();
        long appliances = applianceRepository.count();
        long vendors = vendorRepository.count();

        customReportRepository.deleteAllInBatch();
        dailyReportRepository.deleteAllInBatch();
        metricRepository.deleteAllInBatch();
        applianceRepository.deleteAllInBatch();
        vendorRepository.deleteAllInBatch();

        return Map.of("status", "CLEARED", "removedCustomReports", customReports,
                "removedDailyReports", dailyReports, "removedMetrics", metrics,
                "removedAppliances", appliances, "removedVendors", vendors);
    }
}
