package com.connectedhome.appliances.controller;

import java.util.List;

import com.connectedhome.appliances.model.Vendor;
import com.connectedhome.appliances.dto.CreateVendorRequest;
import com.connectedhome.appliances.vendor.VendorCatalog;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    private final VendorCatalog vendorCatalog;

    public VendorController(VendorCatalog vendorCatalog) {
        this.vendorCatalog = vendorCatalog;
    }

    @GetMapping
    public List<Vendor> findAll() {
        return vendorCatalog.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vendor create(@Valid @RequestBody CreateVendorRequest request) {
        return vendorCatalog.create(request);
    }
}
