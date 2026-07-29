package com.example.minibilling.model.domain;

import java.time.OffsetDateTime;

public record UsageResponse(
        String customerId,
        String customerName,
        OffsetDateTime from,
        OffsetDateTime to,
        double consumption,
        String validationStatus
) {}