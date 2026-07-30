package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.UsageResponse;
import com.example.minibilling.service.UsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usage")
@CrossOrigin(origins = "http://localhost:5173")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping
    public ResponseEntity<List<UsageResponse>> getUsageData() {
        return ResponseEntity.ok(usageService.getUsageData());
    }
}