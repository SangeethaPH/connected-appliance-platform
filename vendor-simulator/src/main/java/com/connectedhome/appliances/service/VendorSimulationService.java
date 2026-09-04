package com.connectedhome.appliances.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.connectedhome.appliances.model.ApplianceStatus;
import com.connectedhome.appliances.model.EmissionMode;
import com.connectedhome.appliances.model.RawVendorMetric;
import com.connectedhome.appliances.model.SimulatedAppliance;
import com.connectedhome.appliances.vendor.MetricGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class VendorSimulationService {
    private final ApplianceRegistry registry;
    private final MetricGenerator generator;
    private final ApplicationEventPublisher publisher;
    private final Clock clock = Clock.systemUTC();

    public VendorSimulationService(ApplianceRegistry registry, MetricGenerator generator,
                                   ApplicationEventPublisher publisher) {
        this.registry = registry;
        this.generator = generator;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${simulator.scheduler-tick-ms:1000}")
    public void emitDueMetrics() {
        Instant now = clock.instant();
        registry.findAll().stream().filter(this::isDue).forEach(appliance -> emit(appliance, now));
    }

    public RawVendorMetric emitNow(String applianceId) {
        SimulatedAppliance appliance = registry.require(applianceId);
        if (appliance.status() == ApplianceStatus.OFFLINE) {
            throw new IllegalArgumentException("Offline appliance cannot emit metrics: " + applianceId);
        }
        return emit(appliance, clock.instant());
    }

    private boolean isDue(SimulatedAppliance appliance) {
        if (appliance.status() == ApplianceStatus.OFFLINE
                || appliance.emissionMode() == EmissionMode.MANUAL_ONLY) {
            return false;
        }
        return appliance.lastMetricAt() == null
                || Duration.between(appliance.lastMetricAt(), clock.instant()).toSeconds() >= appliance.metricIntervalSeconds();
    }

    private RawVendorMetric emit(SimulatedAppliance appliance, Instant timestamp) {
        RawVendorMetric metric = generator.generate(appliance, timestamp);
        publisher.publishEvent(metric);
        registry.markEmitted(appliance.id(), timestamp);
        return metric;
    }
}
