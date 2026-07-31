package com.example.minibilling.model.domain;

public record InvoiceSummary(
        String id,
        String customer,
        String name,
        String period,
        String amount,
        String status
) {}