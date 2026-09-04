package com.connectedhome.appliances.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.connectedhome.appliances.model.ApplianceMetric;
import org.springframework.stereotype.Component;

@Component
public class MetricStore {
    private final CopyOnWriteArrayList<ApplianceMetric> metrics = new CopyOnWriteArrayList<>();

    public void saveAll(List<ApplianceMetric> newMetrics) {
        metrics.addAll(newMetrics);
    }

    public List<ApplianceMetric> findByAppliance(String applianceId) {
        return metrics.stream()
                .filter(metric -> metric.applianceId().equals(applianceId))
                .sorted(Comparator.comparing(ApplianceMetric::capturedAt).reversed())
                .toList();
    }

    public List<ApplianceMetric> findAll() {
        return new ArrayList<>(metrics);
    }

    public int clear() {
        int count = metrics.size();
        metrics.clear();
        return count;
    }
}
