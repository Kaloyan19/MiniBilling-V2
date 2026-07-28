package com.example.minibilling.controllers;

import com.example.minibilling.model.entity.BillingRunEntity;
import com.example.minibilling.service.BillingRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
@CrossOrigin(origins = "http://localhost:5173")
public class BillingRunController {

    private final BillingRunService billingRunService;

    public BillingRunController(BillingRunService billingRunService) {
        this.billingRunService = billingRunService;
    }

    @PostMapping("/run")
    public ResponseEntity<BillingRunService.BillingRunResult> run() {
        BillingRunService.BillingRunResult result = billingRunService.run();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/last")
    public ResponseEntity<BillingRunEntity> getLastBillingRun() {
        BillingRunEntity last = billingRunService.getLastBillingRun();
        if (last == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(last);
    }
}