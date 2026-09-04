package com.connectedhome.infra.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectedhome.infra.entity.DailyReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportRepository extends JpaRepository<DailyReportEntity, UUID> {
    Optional<DailyReportEntity> findByReportDate(LocalDate reportDate);

    List<DailyReportEntity> findAllByOrderByReportDateDesc();
}
