package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.SecurityAlertEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.javaguard.ips.persistence.projection.SeverityCountProjection;
import com.javaguard.ips.persistence.projection.SourceIpCountProjection;
import com.javaguard.ips.persistence.projection.ThreatTypeCountProjection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlertEntity, UUID>,
        JpaSpecificationExecutor<SecurityAlertEntity> {

    List<SecurityAlertEntity>
    findTop100ByOrderByDetectedAtDesc();


    @Query("""
        SELECT
            alert.type AS type,
            COUNT(alert) AS count
        FROM SecurityAlertEntity alert
        GROUP BY alert.type
        ORDER BY COUNT(alert) DESC
        """)
    List<ThreatTypeCountProjection> countByThreatType();


    @Query("""
        SELECT
            alert.severity AS severity,
            COUNT(alert) AS count
        FROM SecurityAlertEntity alert
        GROUP BY alert.severity
        ORDER BY COUNT(alert) DESC
        """)
    List<SeverityCountProjection> countBySeverity();


    @Query("""
        SELECT
            alert.sourceIp AS sourceIp,
            COUNT(alert) AS count
        FROM SecurityAlertEntity alert
        GROUP BY alert.sourceIp
        ORDER BY COUNT(alert) DESC
        """)
    List<SourceIpCountProjection> findTopSourceIps(
            Pageable pageable
    );


    long countByDetectedAtAfter(
            Instant timestamp
    );

}