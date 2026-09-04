package com.connectedhome.infra.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectedhome.infra.entity.ApplianceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplianceRepository extends JpaRepository<ApplianceEntity, UUID> {
    Optional<ApplianceEntity> findByVendorIdAndExternalId(UUID vendorId, String externalId);
    List<ApplianceEntity> findAllByVendorId(UUID vendorId);
}
