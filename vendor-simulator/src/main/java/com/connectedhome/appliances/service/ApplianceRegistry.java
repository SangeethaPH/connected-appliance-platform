package com.connectedhome.appliances.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.connectedhome.appliances.dto.CreateApplianceRequest;
import com.connectedhome.appliances.exception.ResourceNotFoundException;
import com.connectedhome.appliances.model.ApplianceStatus;
import com.connectedhome.appliances.model.SimulatedAppliance;
import com.connectedhome.appliances.vendor.VendorCatalog;
import org.springframework.stereotype.Service;

@Service
public class ApplianceRegistry {
    private final ConcurrentHashMap<String, SimulatedAppliance> appliances = new ConcurrentHashMap<>();
    private final VendorCatalog vendorCatalog;

    public ApplianceRegistry(VendorCatalog vendorCatalog) {
        this.vendorCatalog = vendorCatalog;
    }

    public SimulatedAppliance create(CreateApplianceRequest request) {
        vendorCatalog.require(request.vendorId());
        String id = UUID.randomUUID().toString();
        SimulatedAppliance appliance = new SimulatedAppliance(
                id, request.vendorId().toLowerCase(), request.name(), request.type(),
                ApplianceStatus.ONLINE, request.emissionMode(), request.metricIntervalSeconds(), null);
        appliances.put(id, appliance);
        return appliance;
    }

    public List<SimulatedAppliance> findAll() {
        return appliances.values().stream().sorted(Comparator.comparing(SimulatedAppliance::name)).toList();
    }

    public SimulatedAppliance require(String id) {
        SimulatedAppliance appliance = appliances.get(id);
        if (appliance == null) {
            throw new ResourceNotFoundException("Appliance not found: " + id);
        }
        return appliance;
    }

    public SimulatedAppliance updateStatus(String id, ApplianceStatus status) {
        return appliances.computeIfPresent(id, (key, appliance) -> appliance.withStatus(status)) != null
                ? appliances.get(id)
                : require(id);
    }

    public SimulatedAppliance updateEmissionMode(String id, com.connectedhome.appliances.model.EmissionMode mode) {
        return appliances.computeIfPresent(id, (key, appliance) -> appliance.withEmissionMode(mode)) != null
                ? appliances.get(id)
                : require(id);
    }

    public void markEmitted(String id, Instant timestamp) {
        appliances.computeIfPresent(id, (key, appliance) -> appliance.emittedAt(timestamp));
    }

    public int clear() {
        int count = appliances.size();
        appliances.clear();
        return count;
    }
}
