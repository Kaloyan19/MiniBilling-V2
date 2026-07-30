package com.example.minibilling.service;

import com.example.minibilling.model.domain.Invoice;
import com.example.minibilling.model.domain.User;
import com.example.minibilling.model.entity.BillingRunEntity;
import com.example.minibilling.model.entity.Severity;
import com.example.minibilling.repository.UserRepository;
import com.example.minibilling.repository.jpa.BillingRunEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class BillingRunService {

    private static final ZoneId SOFIA = ZoneId.of("Europe/Sofia");
    private volatile String runStatus = "NOT_STARTED";

    private final UserRepository userRepository;
    private final BillingService billingService;
    private final BillingRunEntityRepository billingRunRepository;
    private final ErrorLogService errorLogService;

    public BillingRunService(UserRepository userRepository,
                             BillingService billingService,
                             BillingRunEntityRepository billingRunRepository,
                             ErrorLogService errorLogService) {
        this.userRepository = userRepository;
        this.billingService = billingService;
        this.billingRunRepository = billingRunRepository;
        this.errorLogService = errorLogService;
    }

    public BillingRunResult run() {
        LocalDate from = getLastBillingRunDate();
        LocalDate to = LocalDate.now();
        BillingRunResult result = processUsers(from, to, null);
        saveBillingRun(from, result.success(), result.failed(), result.skipped());
        return result;
    }

    public void runWithProgress(SseEmitter emitter) {
        runStatus = "IN_PROGRESS";
        new Thread(() -> {
            try {
                LocalDate from = getLastBillingRunDate();
                LocalDate to = LocalDate.now();

                BillingRunResult result = processUsers(from, to, data -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .data("{\"progress\":" + data[0] +
                                        ",\"success\":" + data[1] +
                                        ",\"failed\":" + data[2] +
                                        ",\"skipped\":" + data[3] + "}"));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });

                if (!"NOT_STARTED".equals(runStatus)) {
                    saveBillingRun(from, result.success(), result.failed(), result.skipped());
                    runStatus = "COMPLETED";
                    emitter.send(SseEmitter.event()
                            .data("{\"progress\":100,\"success\":" + result.success() +
                                    ",\"failed\":" + result.failed() +
                                    ",\"skipped\":" + result.skipped() + "}"));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
    }

    public BillingRunEntity getLastBillingRun() {
        return billingRunRepository.findTopByOrderByEndDateDesc();
    }

    public void pause() { runStatus = "PAUSED"; }

    public void resume() { runStatus = "IN_PROGRESS"; }

    public void restart() { runStatus = "NOT_STARTED"; }

    public String getRunStatus() { return runStatus; }

    private LocalDate getLastBillingRunDate() {
        BillingRunEntity last = billingRunRepository.findTopByOrderByEndDateDesc();
        if (last == null) return LocalDate.of(2000, 1, 1);
        return last.getEndDate().toLocalDate();
    }

    private BillingRunResult processUsers(LocalDate from, LocalDate to, Consumer<int[]> onProgress) {
        List<User> users = userRepository.findAll();
        int total = users.size();
        int success = 0, failed = 0, skipped = 0;

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            try {
                Optional<Invoice> invoice = billingService
                        .generateAndSaveInvoice(user.reference(), from, to);
                if (invoice.isPresent()) success++;
                else {
                    skipped++;
                    errorLogService.log("NO_READINGS", "Няма достатъчно отчети",
                            user.reference(), "BillingRunService", Severity.WARNING);
                }
            } catch (Exception e) {
                failed++;
                errorLogService.log("BILLING_ERROR", e.getMessage(),
                        user.reference(), "BillingRunService", Severity.ERROR);
            }

            if (onProgress != null) {
                int progress = (int) ((i + 1) * 100.0 / total);
                onProgress.accept(new int[]{progress, success, failed, skipped});
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            while ("PAUSED".equals(runStatus)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if ("NOT_STARTED".equals(runStatus)) break;
        }
        return new BillingRunResult(success, failed, skipped);
    }

    private void saveBillingRun(LocalDate from, int success, int failed, int skipped) {
        BillingRunEntity entity = new BillingRunEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setEndDate(OffsetDateTime.now(SOFIA));
        entity.setStartDate(from.atStartOfDay().atZone(SOFIA).toOffsetDateTime());
        entity.setStatus("COMPLETED");
        entity.setSuccessCount(success);
        entity.setFailedCount(failed);
        entity.setSkippedCount(skipped);
        billingRunRepository.save(entity);
    }

    public record BillingRunResult(int success, int failed, int skipped) {}
}