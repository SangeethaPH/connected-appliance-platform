package com.connectedhome.infra.entity;

import java.time.Instant;
import java.util.UUID;

import com.connectedhome.infra.model.AuthenticationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendors")
public class VendorEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 30)
    private AuthenticationType authType;

    @Column(name = "auth_username", nullable = false, length = 200)
    private String authUsername;

    @Column(name = "auth_password_encrypted", nullable = false, length = 1000)
    private String authPasswordEncrypted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected VendorEntity() {
    }

    public VendorEntity(UUID id, String code, String name, String baseUrl, AuthenticationType authType,
                        String authUsername, String authPasswordEncrypted, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.baseUrl = baseUrl;
        this.authType = authType;
        this.authUsername = authUsername;
        this.authPasswordEncrypted = authPasswordEncrypted;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBaseUrl() { return baseUrl; }
    public AuthenticationType getAuthType() { return authType; }
    public String getAuthUsername() { return authUsername; }
    public String getAuthPasswordEncrypted() { return authPasswordEncrypted; }
    public Instant getCreatedAt() { return createdAt; }
}
