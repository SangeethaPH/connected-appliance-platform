package com.connectedhome.infra.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_reports")
public class DailyReportEntity {
    @Id
    private UUID id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "zone_id", nullable = false, length = 80)
    private String zoneId;

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

    protected DailyReportEntity() {
    }

    public DailyReportEntity(UUID id, LocalDate reportDate, String zoneId, Instant periodStart,
                             Instant periodEnd, Instant generatedAt, long totalSamples, String reportContent) {
        this.id = id;
        this.reportDate = reportDate;
        this.zoneId = zoneId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generatedAt = generatedAt;
        this.totalSamples = totalSamples;
        this.reportContent = reportContent;
    }

    public UUID getId() { return id; }
    public LocalDate getReportDate() { return reportDate; }
    public String getZoneId() { return zoneId; }
    public Instant getPeriodStart() { return periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getTotalSamples() { return totalSamples; }
    public String getReportContent() { return reportContent; }
}
