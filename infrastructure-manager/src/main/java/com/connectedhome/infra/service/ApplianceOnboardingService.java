package com.connectedhome.infra.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.connector.VendorApplianceSnapshot;
import com.connectedhome.infra.connector.VendorConnector;
import com.connectedhome.infra.dto.ApplianceResponse;
import com.connectedhome.infra.entity.ApplianceEntity;
import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.exception.ConflictException;
import com.connectedhome.infra.exception.ResourceNotFoundException;
import com.connectedhome.infra.model.ApplianceStatus;
import com.connectedhome.infra.repository.ApplianceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplianceOnboardingService {
    private final VendorService vendorService;
    private final VendorConnector vendorConnector;
    private final ApplianceRepository repository;

    public ApplianceOnboardingService(VendorService vendorService, VendorConnector vendorConnector,
                                      ApplianceRepository repository) {
        this.vendorService = vendorService;
        this.vendorConnector = vendorConnector;
        this.repository = repository;
    }

    @Transactional
    public ApplianceResponse onboard(UUID vendorId, String externalApplianceId) {
        VendorEntity vendor = vendorService.require(vendorId);
        if (repository.findByVendorIdAndExternalId(vendorId, externalApplianceId).isPresent()) {
            throw new ConflictException("Appliance is already onboarded: " + externalApplianceId);
        }

        VendorApplianceSnapshot remote = vendorConnector.fetchAppliance(vendor, externalApplianceId);
        Instant now = Instant.now();
        ApplianceEntity entity = new ApplianceEntity(UUID.randomUUID(), vendorId, remote.id(), remote.name(),
                remote.type(), parseStatus(remote.status()), now, now);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ApplianceResponse> findAll(UUID vendorId) {
        List<ApplianceEntity> appliances = vendorId == null ? repository.findAll() : repository.findAllByVendorId(vendorId);
        return appliances.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplianceResponse findOne(UUID id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found: " + id));
    }

    private ApplianceStatus parseStatus(String value) {
        try {
            return ApplianceStatus.valueOf(value.toUpperCase());
        } catch (Exception ignored) {
            return ApplianceStatus.UNKNOWN;
        }
    }

    private ApplianceResponse toResponse(ApplianceEntity entity) {
        return new ApplianceResponse(entity.getId(), entity.getVendorId(), entity.getExternalId(), entity.getName(),
                entity.getType(), entity.getStatus(), entity.getOnboardedAt(), entity.getLastSeenAt());
    }
}
