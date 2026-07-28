package com.example.minibilling.service;

import com.example.minibilling.model.domain.Invoice;
import com.example.minibilling.model.domain.User;
import com.example.minibilling.model.entity.BillingRunEntity;
import com.example.minibilling.repository.UserRepository;
import com.example.minibilling.repository.jpa.BillingRunEntityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingRunService {

    private final UserRepository userRepository;
    private final BillingService billingService;
    private final BillingRunEntityRepository billingRunRepository;

    public BillingRunService(UserRepository userRepository,
                             BillingService billingService,
                             BillingRunEntityRepository billingRunRepository) {
        this.userRepository = userRepository;
        this.billingService = billingService;
        this.billingRunRepository = billingRunRepository;
    }

    public BillingRunResult run() {
        LocalDate from = getLastBillingRunDate();
        LocalDate to = LocalDate.now();

        int success = 0;
        int failed = 0;
        int skipped = 0;

        for (User user : userRepository.findAll()) {
            try {
                Optional<Invoice> invoice = billingService
                        .generateAndSaveInvoice(user.reference(), from, to);
                if (invoice.isPresent()) success++;
                else skipped++;
            } catch (Exception e) {
                failed++;
            }
        }

        saveBillingRun(from, to, success, failed);
        return new BillingRunResult(success, failed, skipped);
    }

    public BillingRunEntity getLastBillingRun() {
        return billingRunRepository.findTopByOrderByEndDateDesc();
    }

    private LocalDate getLastBillingRunDate() {
        BillingRunEntity last = billingRunRepository.findTopByOrderByEndDateDesc();
        if (last == null) {
            return LocalDate.of(2000, 1, 1);
        }
        return last.getEndDate().toLocalDate();
    }

    private void saveBillingRun(LocalDate from, LocalDate to, int success, int failed) {
        BillingRunEntity entity = new BillingRunEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setStartDate(from.atStartOfDay().atOffset(java.time.ZoneOffset.UTC));
        entity.setEndDate(to.atStartOfDay().atOffset(java.time.ZoneOffset.UTC));
        entity.setStatus("COMPLETED");
        entity.setSuccessCount(success);
        entity.setFailedCount(failed);
        billingRunRepository.save(entity);
    }

    public record BillingRunResult(int success, int failed, int skipped) {}
}