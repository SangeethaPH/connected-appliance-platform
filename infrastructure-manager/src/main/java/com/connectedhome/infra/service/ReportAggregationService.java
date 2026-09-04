package com.connectedhome.infra.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.connectedhome.infra.dto.ApplianceDailySummary;
import com.connectedhome.infra.dto.MetricAggregate;
import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.MetricEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.repository.ApplianceRepository;
import com.connectedhome.infra.repository.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportAggregationService {
    private final ApplianceRepository applianceRepository;
    private final VendorRepository vendorRepository;

    public ReportAggregationService(ApplianceRepository applianceRepository, VendorRepository vendorRepository) {
        this.applianceRepository = applianceRepository;
        this.vendorRepository = vendorRepository;
    }

    public List<ApplianceDailySummary> summarize(List<MetricEntity> samples) {
        Map<UUID, ApplianceEntity> appliances = applianceRepository
                .findAllById(samples.stream().map(MetricEntity::getApplianceId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(ApplianceEntity::getId, appliance -> appliance));
        Map<UUID, Map<String, List<MetricEntity>>> grouped = samples.stream().collect(Collectors.groupingBy(
                MetricEntity::getApplianceId, LinkedHashMap::new,
                Collectors.groupingBy(MetricEntity::getMetricName, LinkedHashMap::new, Collectors.toList())));

        List<ApplianceDailySummary> summaries = new ArrayList<>();
        grouped.forEach((applianceId, byMetric) -> {
            ApplianceEntity appliance = appliances.get(applianceId);
            VendorEntity vendor = appliance == null ? null : vendorRepository.findById(appliance.getVendorId()).orElse(null);
            List<MetricAggregate> aggregates = byMetric.entrySet().stream()
                    .map(entry -> aggregate(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparing(MetricAggregate::metricName)).toList();
            summaries.add(new ApplianceDailySummary(applianceId,
                    appliance == null ? "unknown" : appliance.getExternalId(),
                    appliance == null ? "unknown" : appliance.getName(),
                    appliance == null ? "unknown" : appliance.getType(),
                    appliance == null ? null : appliance.getVendorId(),
                    vendor == null ? "unknown" : vendor.getCode(),
                    vendor == null ? "unknown" : vendor.getName(), aggregates));
        });
        return summaries.stream().sorted(Comparator.comparing(ApplianceDailySummary::applianceName)).toList();
    }

    private MetricAggregate aggregate(String metricName, List<MetricEntity> samples) {
        DoubleSummaryStatistics statistics = samples.stream().mapToDouble(MetricEntity::getValue).summaryStatistics();
        return new MetricAggregate(metricName, samples.getFirst().getUnit(), statistics.getCount(),
                round(statistics.getMin()), round(statistics.getMax()), round(statistics.getAverage()));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
