package com.connectedhome.appliances.controller;

import java.util.Map;

import com.connectedhome.appliances.service.ApplianceRegistry;
import com.connectedhome.appliances.service.MetricStore;
import com.connectedhome.appliances.vendor.VendorCatalog;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulator/data")
public class SimulatorDataController {
    private final ApplianceRegistry applianceRegistry;
    private final MetricStore metricStore;
    private final VendorCatalog vendorCatalog;

    public SimulatorDataController(ApplianceRegistry applianceRegistry, MetricStore metricStore,
                                   VendorCatalog vendorCatalog) {
        this.applianceRegistry = applianceRegistry;
        this.metricStore = metricStore;
        this.vendorCatalog = vendorCatalog;
    }

    @DeleteMapping
    public Map<String, Object> reset() {
        int metrics = metricStore.clear();
        int appliances = applianceRegistry.clear();
        int vendors = vendorCatalog.reset();
        return Map.of("status", "RESET", "removedMetrics", metrics,
                "removedAppliances", appliances, "removedVendors", vendors,
                "defaultVendorsRestored", 2);
    }
}
