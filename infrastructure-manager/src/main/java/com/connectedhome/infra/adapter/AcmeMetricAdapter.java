package com.connectedhome.infra.adapter;

import java.util.List;

import com.connectedhome.infra.messaging.RawMetricEvent;
import org.springframework.stereotype.Component;

@Component
public class AcmeMetricAdapter implements VendorMetricAdapter {
    @Override
    public boolean supports(String vendorCode) {
        return "acme".equalsIgnoreCase(vendorCode);
    }

    @Override
    public List<NormalizedMetric> normalize(RawMetricEvent event) {
        return event.metrics().entrySet().stream()
                .map(entry -> switch (entry.getKey()) {
                    case "power_watts" -> metric("POWER", entry.getValue(), "W");
                    case "internal_temp_c" -> metric("INTERNAL_TEMPERATURE", entry.getValue(), "C");
                    case "room_temp_c" -> metric("ROOM_TEMPERATURE", entry.getValue(), "C");
                    case "cavity_temp_c" -> metric("CAVITY_TEMPERATURE", entry.getValue(), "C");
                    case "cycle_progress_pct" -> metric("CYCLE_PROGRESS", entry.getValue(), "%");
                    case "volume_pct" -> metric("VOLUME", entry.getValue(), "%");
                    case "fan_speed_pct" -> metric("FAN_SPEED", entry.getValue(), "%");
                    default -> metric(entry.getKey().toUpperCase(), entry.getValue(), "unknown");
                })
                .toList();
    }

    private NormalizedMetric metric(String name, double value, String unit) {
        return new NormalizedMetric(name, value, unit);
    }
}
