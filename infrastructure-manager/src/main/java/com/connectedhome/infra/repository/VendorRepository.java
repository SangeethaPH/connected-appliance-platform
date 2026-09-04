package com.connectedhome.infra.repository;

import java.util.Optional;
import java.util.UUID;

import com.connectedhome.infra.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<VendorEntity, UUID> {
    Optional<VendorEntity> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
