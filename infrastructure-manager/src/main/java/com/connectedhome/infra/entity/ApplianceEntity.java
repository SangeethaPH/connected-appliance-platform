package com.connectedhome.infra.entity;

import java.time.Instant;
import java.util.UUID;

import com.connectedhome.infra.model.ApplianceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appliances")
public class ApplianceEntity {
    @Id
    private UUID id;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "external_id", nullable = false, length = 200)
    private String externalId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 80)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplianceStatus status;

    @Column(name = "onboarded_at", nullable = false)
    private Instant onboardedAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    protected ApplianceEntity() {
    }

    public ApplianceEntity(UUID id, UUID vendorId, String externalId, String name, String type,
                           ApplianceStatus status, Instant onboardedAt, Instant lastSeenAt) {
        this.id = id;
        this.vendorId = vendorId;
        this.externalId = externalId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.onboardedAt = onboardedAt;
        this.lastSeenAt = lastSeenAt;
    }

    public UUID getId() { return id; }
    public UUID getVendorId() { return vendorId; }
    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public ApplianceStatus getStatus() { return status; }
    public Instant getOnboardedAt() { return onboardedAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
}
