package com.connectedhome.infra.controller;

import java.util.Map;

import com.connectedhome.infra.service.DemoDataResetService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/demo-data")
public class DemoDataController {
    private final DemoDataResetService resetService;

    public DemoDataController(DemoDataResetService resetService) {
        this.resetService = resetService;
    }

    @DeleteMapping
    public Map<String, Object> clearAll() {
        return resetService.clearAll();
    }
}
