package com.connectedhome.infra.adapter;

import java.util.List;

import com.connectedhome.infra.messaging.RawMetricEvent;
import org.springframework.stereotype.Component;

@Component
public class GlobexMetricAdapter implements VendorMetricAdapter {
    @Override
    public boolean supports(String vendorCode) {
        return "globex".equalsIgnoreCase(vendorCode);
    }

    @Override
    public List<NormalizedMetric> normalize(RawMetricEvent event) {
        return event.metrics().entrySet().stream()
                .map(entry -> switch (entry.getKey()) {
                    case "energy_kw" -> metric("POWER", entry.getValue() * 1000.0, "W");
                    case "cabinet_temperature_f" -> metric(
                            "INTERNAL_TEMPERATURE", fahrenheitToCelsius(entry.getValue()), "C");
                    case "ambient_temperature_f" -> metric(
                            "ROOM_TEMPERATURE", fahrenheitToCelsius(entry.getValue()), "C");
                    case "oven_temperature_f" -> metric(
                            "CAVITY_TEMPERATURE", fahrenheitToCelsius(entry.getValue()), "C");
                    case "program_completion_ratio" -> metric("CYCLE_PROGRESS", entry.getValue() * 100.0, "%");
                    case "audio_level_ratio" -> metric("VOLUME", entry.getValue() * 100.0, "%");
                    case "motor_speed_ratio" -> metric("FAN_SPEED", entry.getValue() * 100.0, "%");
                    default -> metric(entry.getKey().toUpperCase(), entry.getValue(), "unknown");
                })
                .toList();
    }

    private double fahrenheitToCelsius(double fahrenheit) {
        return Math.round(((fahrenheit - 32.0) * 5.0 / 9.0) * 100.0) / 100.0;
    }

    private NormalizedMetric metric(String name, double value, String unit) {
        return new NormalizedMetric(name, Math.round(value * 100.0) / 100.0, unit);
    }
}
