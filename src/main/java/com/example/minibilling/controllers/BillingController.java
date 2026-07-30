package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.Invoice;
import com.example.minibilling.model.domain.InvoiceSummary;
import com.example.minibilling.repository.InvoiceRepository;
import com.example.minibilling.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "http://localhost:3000")
public class BillingController {

    private final BillingService billingService;
    private final InvoiceRepository invoiceRepository;

    public BillingController(BillingService billingService, InvoiceRepository invoiceRepository){
        this.billingService = billingService;
        this.invoiceRepository = invoiceRepository;
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
    public ResponseEntity<List<InvoiceSummary>> listInvoices() {
        return ResponseEntity.ok(invoiceRepository.findAllSummaries());
    }

}
