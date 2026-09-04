package com.connectedhome.infra.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "custom_reports")
public class CustomReportEntity {
    @Id
    private UUID id;
    @Column(name = "period_start", nullable = false)
    private Instant periodStart;
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
    @Column(name = "total_samples", nullable = false)
    private long totalSamples;
    @Column(name = "report_content", nullable = false, columnDefinition = "TEXT")
    private String reportContent;

    protected CustomReportEntity() {
    }

    public CustomReportEntity(UUID id, Instant periodStart, Instant periodEnd, Instant generatedAt,
                              long totalSamples, String reportContent) {
        this.id = id;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generatedAt = generatedAt;
        this.totalSamples = totalSamples;
        this.reportContent = reportContent;
    }

    public UUID getId() { return id; }
    public Instant getPeriodStart() { return periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getTotalSamples() { return totalSamples; }
    public String getReportContent() { return reportContent; }
}
