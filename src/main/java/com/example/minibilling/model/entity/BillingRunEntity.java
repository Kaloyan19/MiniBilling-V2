package com.example.minibilling.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "billing_runs")
public class BillingRunEntity {

    @Id
    private String id;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    private String status;

    @Column(name = "success_count")
    private int successCount;

    @Column(name = "failed_count")
    private int failedCount;

    @Column(name = "skipped_count")
    private int skippedCount;

    public BillingRunEntity() {}

    public String getId() { return id; }
    public OffsetDateTime getStartDate() { return startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public int getSuccessCount() { return successCount; }
    public int getFailedCount() { return failedCount; }
    public int getSkippedCount() { return skippedCount; }

    public void setId(String id) { this.id = id; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    public void setStatus(String status) { this.status = status; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
}