package com.connectedhome.infra.controller;

import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.ApplianceResponse;
import com.connectedhome.infra.dto.CreateVendorRequest;
import com.connectedhome.infra.dto.OnboardApplianceRequest;
import com.connectedhome.infra.dto.VendorResponse;
import com.connectedhome.infra.service.ApplianceOnboardingService;
import com.connectedhome.infra.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    private final VendorService vendorService;
    private final ApplianceOnboardingService onboardingService;

    public VendorController(VendorService vendorService, ApplianceOnboardingService onboardingService) {
        this.vendorService = vendorService;
        this.onboardingService = onboardingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse create(@Valid @RequestBody CreateVendorRequest request) {
        return vendorService.create(request);
    }

    @GetMapping
    public List<VendorResponse> findAll() {
        return vendorService.findAll();
    }

    @PostMapping("/{vendorId}/appliances")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplianceResponse onboard(@PathVariable UUID vendorId,
                                     @Valid @RequestBody OnboardApplianceRequest request) {
        return onboardingService.onboard(vendorId, request.externalApplianceId());
    }
}
