package com.connectedhome.appliances.vendor;

import java.util.List;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import com.connectedhome.appliances.dto.CreateVendorRequest;
import com.connectedhome.appliances.exception.ConflictException;
import com.connectedhome.appliances.exception.ResourceNotFoundException;
import com.connectedhome.appliances.model.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VendorCatalog {
    private final ConcurrentHashMap<String, Vendor> vendors = new ConcurrentHashMap<>();

    public VendorCatalog() {
        restoreDefaults();
    }

    public List<Vendor> findAll() {
        return vendors.values().stream().sorted(Comparator.comparing(Vendor::name)).toList();
    }

    public Vendor create(CreateVendorRequest request) {
        String id = request.id().trim().toLowerCase();
        Vendor vendor = new Vendor(id, request.name(), request.apiStyle(), request.authentication(),
                request.metricProfile());
        if (vendors.putIfAbsent(id, vendor) != null) {
            throw new ConflictException("Vendor already exists: " + id);
        }
        return vendor;
    }

    public Vendor require(String id) {
        Vendor vendor = vendors.get(id.trim().toLowerCase());
        if (vendor == null) {
            throw new ResourceNotFoundException("Vendor not found: " + id);
        }
        return vendor;
    }

    public int reset() {
        int count = vendors.size();
        vendors.clear();
        restoreDefaults();
        return count;
    }

    private void restoreDefaults() {
        vendors.put("acme", new Vendor("acme", "Acme Smart Home", "REST/JSON", "API_KEY", "ACME"));
        vendors.put("globex", new Vendor("globex", "Globex Appliances", "REST/JSON", "OAUTH2", "GLOBEX"));
    }
}
