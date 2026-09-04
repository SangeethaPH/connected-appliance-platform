package com.connectedhome.infra.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedhome.infra.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<MetricEntity, UUID> {
    boolean existsByEventId(UUID eventId);

    List<MetricEntity> findAllByApplianceIdAndCapturedAtBetweenOrderByCapturedAtAsc(
            UUID applianceId, Instant from, Instant to);

    List<MetricEntity> findAllByCapturedAtGreaterThanEqualAndCapturedAtLessThanOrderByCapturedAtAsc(
            Instant fromInclusive, Instant toExclusive);
}
