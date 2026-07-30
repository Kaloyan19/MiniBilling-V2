package com.example.minibilling.controllers;

import com.example.minibilling.service.InvoicePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "http://localhost:5173")
public class InvoicePdfController {

    private final InvoicePdfService invoicePdfService;

    public InvoicePdfController(InvoicePdfService invoicePdfService) {
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String id) {
        try {
            byte[] pdf = invoicePdfService.generatePdf(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"invoice-" + id + ".pdf\"")
                    .body(pdf);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
