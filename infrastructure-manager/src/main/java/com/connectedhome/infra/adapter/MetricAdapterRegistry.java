package com.connectedhome.infra.adapter;

import java.util.List;

import com.connectedhome.infra.exception.UnsupportedVendorMetricException;
import org.springframework.stereotype.Component;

@Component
public class MetricAdapterRegistry {
    private final List<VendorMetricAdapter> adapters;

    public MetricAdapterRegistry(List<VendorMetricAdapter> adapters) {
        this.adapters = List.copyOf(adapters);
    }

    public VendorMetricAdapter require(String vendorCode) {
        return adapters.stream()
                .filter(adapter -> adapter.supports(vendorCode))
                .findFirst()
                .orElseThrow(() -> new UnsupportedVendorMetricException(
                        "No metric adapter registered for vendor: " + vendorCode));
    }

    public VendorMetricAdapter require(String vendorCode, String metricProfile) {
        try {
            return require(vendorCode);
        } catch (UnsupportedVendorMetricException exception) {
            if (metricProfile == null || metricProfile.isBlank()) {
                throw exception;
            }
            return require(metricProfile);
        }
    }
}
