package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.Invoice;
import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.repository.InvoiceRepository;
import com.example.minibilling.repository.jpa.AccountEntityRepository;
import com.example.minibilling.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.minibilling.model.domain.InvoiceSummary;
import java.util.List;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "http://localhost:5173")
public class BillingController {

    private final BillingService billingService;
    private final InvoiceRepository invoiceRepository;
    private final AccountEntityRepository accountRepository;

    public BillingController(BillingService billingService, InvoiceRepository invoiceRepository, AccountEntityRepository accountRepository){
        this.billingService = billingService;
        this.invoiceRepository = invoiceRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/{reference}")
    public ResponseEntity<Invoice> generateInvoice(
            @PathVariable String reference,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) throws IOException {

        return billingService.generateAndSaveInvoice(reference, from, to)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{reference}")
    public ResponseEntity<Invoice> getInvoice(
            @PathVariable String reference,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        Invoice invoice = invoiceRepository.findByUserReferenceAndPeriod(
                reference, from + "_" + to);
        if (invoice == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(invoice);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceSummary>> listInvoices(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("");

        if (role.equals("ROLE_ADMIN")) {
            return ResponseEntity.ok(invoiceRepository.findAllSummaries());
        }
        AccountEntity account = accountRepository.findByUsername(authentication.getName());
        return ResponseEntity.ok(invoiceRepository.findSummariesForUser(
                account.getCustomerReference()));
    }

}
