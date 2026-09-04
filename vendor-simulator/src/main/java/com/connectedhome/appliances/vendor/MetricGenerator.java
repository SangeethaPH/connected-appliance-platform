package com.connectedhome.appliances.vendor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.connectedhome.appliances.model.RawVendorMetric;
import com.connectedhome.appliances.model.SimulatedAppliance;
import org.springframework.stereotype.Component;

@Component
public class MetricGenerator {
    private final VendorCatalog vendorCatalog;

    public MetricGenerator(VendorCatalog vendorCatalog) {
        this.vendorCatalog = vendorCatalog;
    }

    public RawVendorMetric generate(SimulatedAppliance appliance, Instant capturedAt) {
        Map<String, Double> canonical = new LinkedHashMap<>();
        canonical.put("power_watts", between(35, 1_800));

        switch (appliance.type()) {
            case REFRIGERATOR -> canonical.put("internal_temp_c", between(2, 7));
            case AIR_CONDITIONER -> canonical.put("room_temp_c", between(18, 30));
            case OVEN -> canonical.put("cavity_temp_c", between(25, 240));
            case WASHER, DRYER -> canonical.put("cycle_progress_pct", between(0, 100));
            case TELEVISION -> canonical.put("volume_pct", between(0, 100));
            case FAN -> canonical.put("fan_speed_pct", between(10, 100));
        }

        String metricProfile = vendorCatalog.require(appliance.vendorId()).metricProfile();
        Map<String, Double> vendorMetrics = metricProfile.equalsIgnoreCase("GLOBEX")
                ? toGlobexMetrics(canonical)
                : canonical;

        return new RawVendorMetric(
                UUID.randomUUID(),
                appliance.vendorId(),
                appliance.id(),
                appliance.type().name(),
                "1.0",
                capturedAt,
                Map.copyOf(vendorMetrics),
                Map.of("simulator", "connected-appliance-vendor-simulator", "metricProfile", metricProfile));
    }

    private Map<String, Double> toGlobexMetrics(Map<String, Double> canonical) {
        Map<String, Double> globex = new LinkedHashMap<>();
        canonical.forEach((name, value) -> {
            switch (name) {
                case "power_watts" -> globex.put("energy_kw", round(value / 1000.0));
                case "internal_temp_c" -> globex.put("cabinet_temperature_f", celsiusToFahrenheit(value));
                case "room_temp_c" -> globex.put("ambient_temperature_f", celsiusToFahrenheit(value));
                case "cavity_temp_c" -> globex.put("oven_temperature_f", celsiusToFahrenheit(value));
                case "cycle_progress_pct" -> globex.put("program_completion_ratio", round(value / 100.0));
                case "volume_pct" -> globex.put("audio_level_ratio", round(value / 100.0));
                case "fan_speed_pct" -> globex.put("motor_speed_ratio", round(value / 100.0));
                default -> globex.put(name, value);
            }
        });
        return globex;
    }

    private double celsiusToFahrenheit(double celsius) {
        return round((celsius * 9.0 / 5.0) + 32.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double between(double minimum, double maximum) {
        return Math.round(ThreadLocalRandom.current().nextDouble(minimum, maximum) * 100.0) / 100.0;
    }
}
