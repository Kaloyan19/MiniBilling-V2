package com.example.minibilling.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "error_logs")
public class ErrorLogEntity {

    @Id
    private String id;

    @Column(name = "occurred_at")
    private OffsetDateTime occurredAt;

    @Column(name = "error_type")
    private String errorType;

    private String description;

    @Column(name = "customer_id")
    private String customerId;

    private String module;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String status;

    public ErrorLogEntity() {}

    public String getId() { return id; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public String getErrorType() { return errorType; }
    public String getDescription() { return description; }
    public String getCustomerId() { return customerId; }
    public String getModule() { return module; }
    public Severity getSeverity() { return severity; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    public void setDescription(String description) { this.description = description; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setModule(String module) { this.module = module; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setStatus(String status) { this.status = status; }
}