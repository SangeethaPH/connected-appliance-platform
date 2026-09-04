package com.connectedhome.infra.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appliance_metrics")
public class MetricEntity {
    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "appliance_id", nullable = false)
    private UUID applianceId;

    @Column(name = "metric_name", nullable = false, length = 120)
    private String metricName;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false, length = 40)
    private String unit;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected MetricEntity() {
    }

    public MetricEntity(UUID id, UUID eventId, UUID applianceId, String metricName, double value,
                        String unit, Instant capturedAt, Instant receivedAt) {
        this.id = id;
        this.eventId = eventId;
        this.applianceId = applianceId;
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.capturedAt = capturedAt;
        this.receivedAt = receivedAt;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getApplianceId() { return applianceId; }
    public String getMetricName() { return metricName; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public Instant getCapturedAt() { return capturedAt; }
    public Instant getReceivedAt() { return receivedAt; }
}
