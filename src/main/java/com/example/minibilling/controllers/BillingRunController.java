package com.example.minibilling.controllers;

import com.example.minibilling.model.entity.BillingRunEntity;
import com.example.minibilling.service.BillingRunService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping(value = "/run/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBillingRun() {
        SseEmitter emitter = new SseEmitter(300000L); // 5 минути timeout
        billingRunService.runWithProgress(emitter);
        return emitter;
    }
}