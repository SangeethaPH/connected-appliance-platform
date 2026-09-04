package com.connectedhome.appliances.controller;

import java.util.List;

import com.connectedhome.appliances.dto.CreateApplianceRequest;
import com.connectedhome.appliances.dto.UpdateStatusRequest;
import com.connectedhome.appliances.dto.UpdateEmissionModeRequest;
import com.connectedhome.appliances.model.ApplianceMetric;
import com.connectedhome.appliances.model.RawVendorMetric;
import com.connectedhome.appliances.model.SimulatedAppliance;
import com.connectedhome.appliances.service.ApplianceRegistry;
import com.connectedhome.appliances.service.MetricStore;
import com.connectedhome.appliances.service.VendorSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulator/appliances")
public class ApplianceController {
    private final ApplianceRegistry registry;
    private final VendorSimulationService simulationService;
    private final MetricStore metricStore;

    public ApplianceController(ApplianceRegistry registry, VendorSimulationService simulationService, MetricStore metricStore) {
        this.registry = registry;
        this.simulationService = simulationService;
        this.metricStore = metricStore;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulatedAppliance create(@Valid @RequestBody CreateApplianceRequest request) {
        return registry.create(request);
    }

    @GetMapping
    public List<SimulatedAppliance> findAll() {
        return registry.findAll();
    }

    @GetMapping("/{id}")
    public SimulatedAppliance findOne(@PathVariable String id) {
        return registry.require(id);
    }

    @PatchMapping("/{id}/status")
    public SimulatedAppliance updateStatus(@PathVariable String id, @Valid @RequestBody UpdateStatusRequest request) {
        return registry.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/emission-mode")
    public SimulatedAppliance updateEmissionMode(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmissionModeRequest request) {
        return registry.updateEmissionMode(id, request.emissionMode());
    }

    @PostMapping("/{id}/emit")
    public RawVendorMetric emit(@PathVariable String id) {
        return simulationService.emitNow(id);
    }

    @GetMapping("/{id}/metrics")
    public List<ApplianceMetric> metrics(@PathVariable String id) {
        registry.require(id);
        return metricStore.findByAppliance(id);
    }
}
