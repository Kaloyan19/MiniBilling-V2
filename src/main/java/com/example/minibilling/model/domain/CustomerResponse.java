package com.example.minibilling.model.domain;

public record CustomerResponse(
        String id,
        String name,
        int tariffPlan,
        String validationStatus
) {}