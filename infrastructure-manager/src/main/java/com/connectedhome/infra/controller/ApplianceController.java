package com.connectedhome.infra.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.ApplianceResponse;
import com.connectedhome.infra.dto.MetricResponse;
import com.connectedhome.infra.service.ApplianceOnboardingService;
import com.connectedhome.infra.service.MetricQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appliances")
public class ApplianceController {
    private final ApplianceOnboardingService onboardingService;
    private final MetricQueryService metricQueryService;

    public ApplianceController(ApplianceOnboardingService onboardingService, MetricQueryService metricQueryService) {
        this.onboardingService = onboardingService;
        this.metricQueryService = metricQueryService;
    }

    @GetMapping
    public List<ApplianceResponse> findAll(@RequestParam(required = false) UUID vendorId) {
        return onboardingService.findAll(vendorId);
    }

    @GetMapping("/{id}")
    public ApplianceResponse findOne(@PathVariable UUID id) {
        return onboardingService.findOne(id);
    }

    @GetMapping("/{id}/metrics")
    public List<MetricResponse> metrics(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return metricQueryService.history(id, from, to);
    }
}
