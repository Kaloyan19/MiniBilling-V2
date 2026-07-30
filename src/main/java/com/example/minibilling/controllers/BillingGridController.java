package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.PricePeriod;
import com.example.minibilling.model.domain.Reading;
import com.example.minibilling.service.BillingGridService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/table")
public class BillingGridController {
    private final BillingGridService billingGridService;

    public BillingGridController(BillingGridService billingGridService) {
        this.billingGridService = billingGridService;
    }

    @GetMapping("/readingInfo")
    public ResponseEntity<List<Reading>> getReadingInfo() {
        return ResponseEntity.ok(billingGridService.getAllReadings());
    }

    @GetMapping("/pricesInfo")
    public ResponseEntity<List<PricePeriod>> getPricesInfo() {
        return ResponseEntity.ok(billingGridService.getAllPricePeriods());
    }
}
