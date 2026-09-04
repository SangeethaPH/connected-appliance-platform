package com.connectedhome.infra.controller;

import java.time.LocalDate;
import java.util.List;

import com.connectedhome.infra.dto.DailyReportResponse;
import com.connectedhome.infra.service.DailyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/daily")
public class DailyReportController {
    private final DailyReportService reportService;

    public DailyReportController(DailyReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{date}")
    public DailyReportResponse generate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.generate(date);
    }

    @GetMapping("/{date}")
    public DailyReportResponse find(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.find(date);
    }

    @GetMapping
    public List<DailyReportResponse> findAll() {
        return reportService.findAll();
    }
}
