package com.connectedhome.infra.repository;

import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.entity.CustomReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomReportRepository extends JpaRepository<CustomReportEntity, UUID> {
    List<CustomReportEntity> findAllByOrderByGeneratedAtDesc();
}
