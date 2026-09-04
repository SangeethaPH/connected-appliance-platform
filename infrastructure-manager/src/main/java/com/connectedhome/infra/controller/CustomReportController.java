package com.connectedhome.infra.controller;

import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.CustomReportRequest;
import com.connectedhome.infra.dto.CustomReportResponse;
import com.connectedhome.infra.service.CustomReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/custom")
public class CustomReportController {
    private final CustomReportService reportService;

    public CustomReportController(CustomReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public CustomReportResponse generate(@Valid @RequestBody CustomReportRequest request) {
        return reportService.generate(request);
    }

    @GetMapping("/{id}")
    public CustomReportResponse find(@PathVariable UUID id) {
        return reportService.find(id);
    }

    @GetMapping
    public List<CustomReportResponse> findAll() {
        return reportService.findAll();
    }
}
