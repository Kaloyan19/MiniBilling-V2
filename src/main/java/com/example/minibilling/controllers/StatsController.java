package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.StatsResponse;
import com.example.minibilling.model.entity.BillingRunEntity;
import com.example.minibilling.repository.jpa.BillingRunEntityRepository;
import com.example.minibilling.repository.jpa.UserEntityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = "http://localhost:5173")
public class StatsController {

    private final UserEntityRepository userEntityRepository;
    private final BillingRunEntityRepository billingRunRepository;

    public StatsController(UserEntityRepository userEntityRepository,
                           BillingRunEntityRepository billingRunRepository) {
        this.userEntityRepository = userEntityRepository;
        this.billingRunRepository = billingRunRepository;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        long totalUsers = userEntityRepository.count();
        BillingRunEntity lastRun = billingRunRepository.findTopByOrderByEndDateDesc();

        if (lastRun == null) {
            return ResponseEntity.ok(new StatsResponse(totalUsers, 0, 0, 0));
        }

        return ResponseEntity.ok(new StatsResponse(
                totalUsers,
                lastRun.getSuccessCount(),
                lastRun.getFailedCount(),
                lastRun.getSkippedCount()
        ));
    }
}