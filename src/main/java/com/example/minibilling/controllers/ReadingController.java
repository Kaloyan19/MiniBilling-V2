package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.ProductType;
import com.example.minibilling.model.entity.ReadingEntity;
import com.example.minibilling.model.entity.UserEntity;
import com.example.minibilling.repository.jpa.AccountEntityRepository;
import com.example.minibilling.repository.jpa.ReadingEntityRepository;
import com.example.minibilling.repository.jpa.UserEntityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/readings")
@CrossOrigin(origins = "http://localhost:5173")
public class ReadingController {

    private final ReadingEntityRepository readingEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final AccountEntityRepository accountEntityRepository;

    public ReadingController(ReadingEntityRepository readingEntityRepository,
                             UserEntityRepository userEntityRepository,
                             AccountEntityRepository accountEntityRepository) {
        this.readingEntityRepository = readingEntityRepository;
        this.userEntityRepository = userEntityRepository;
        this.accountEntityRepository = accountEntityRepository;
    }

    @PostMapping("/self-report")
    public ResponseEntity<String> selfReport(
            @RequestBody SelfReportRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        var account = accountEntityRepository.findByUsername(username);
        if (account.getCustomerReference() == null) {
            return ResponseEntity.badRequest().body("Нямате свързан клиентски акаунт!");
        }

        UserEntity user = userEntityRepository.findByReference(account.getCustomerReference());
        if (user == null) {
            return ResponseEntity.badRequest().body("Клиентът не е намерен!");
        }

        ReadingEntity entity = new ReadingEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setUser(user);
        entity.setProduct(ProductType.valueOf(request.product().toUpperCase()));
        entity.setDateTime(request.dateTime());
        entity.setLastReading(request.meterReading());
        entity.setInvoiced(false);
        entity.setSelfReported(true);

        readingEntityRepository.save(entity);
        return ResponseEntity.ok("Самоотчетът е записан успешно!");
    }

    public record SelfReportRequest(
            String product,
            OffsetDateTime dateTime,
            BigDecimal meterReading
    ) {}
}