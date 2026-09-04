package com.connectedhome.appliances.service;

import java.util.List;
import java.util.Map;

import com.connectedhome.appliances.model.ApplianceMetric;
import com.connectedhome.appliances.model.RawVendorMetric;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MetricIngestionService {
    private static final Map<String, String> UNITS = Map.of(
            "power_watts", "W",
            "internal_temp_c", "C",
            "room_temp_c", "C",
            "cavity_temp_c", "C",
            "cycle_progress_pct", "%",
            "volume_pct", "%");

    private final MetricStore metricStore;

    public MetricIngestionService(MetricStore metricStore) {
        this.metricStore = metricStore;
    }

    @EventListener
    public void ingest(RawVendorMetric event) {
        List<ApplianceMetric> normalized = event.metrics().entrySet().stream()
                .map(entry -> new ApplianceMetric(event.externalApplianceId(), event.vendorCode(), entry.getKey(),
                        entry.getValue(), UNITS.getOrDefault(entry.getKey(), "unknown"), event.capturedAt()))
                .toList();
        metricStore.saveAll(normalized);
    }
}
