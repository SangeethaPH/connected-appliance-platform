package com.connectedhome.infra.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.dto.CreateVendorRequest;
import com.connectedhome.infra.dto.VendorResponse;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.exception.ConflictException;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.model.AuthenticationType;
import com.connectedhome.infra.repository.VendorRepository;
import com.connectedhome.infra.security.CredentialCipher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {
    private final VendorRepository repository;
    private final CredentialCipher credentialCipher;

    public VendorService(VendorRepository repository, CredentialCipher credentialCipher) {
        this.repository = repository;
        this.credentialCipher = credentialCipher;
    }

    @Transactional
    public VendorResponse create(CreateVendorRequest request) {
        if (repository.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("Vendor code already exists: " + request.code());
        }
        VendorEntity saved = repository.save(new VendorEntity(UUID.randomUUID(), request.code().toLowerCase(),
                request.name(), stripTrailingSlash(request.baseUrl()), AuthenticationType.BASIC,
                request.username(), credentialCipher.encrypt(request.password()), Instant.now()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendorEntity require(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
    }

    private VendorResponse toResponse(VendorEntity entity) {
        return new VendorResponse(entity.getId(), entity.getCode(), entity.getName(), entity.getBaseUrl(),
                entity.getAuthType(), entity.getAuthUsername(), entity.getCreatedAt());
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
