package com.javaguard.ips.persistence;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.persistence.entity.SecurityAlertEntity;
import com.javaguard.ips.persistence.repository.SecurityAlertRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static com.javaguard.ips.persistence.specification.SecurityAlertSpecifications.hasDestinationIp;
import static com.javaguard.ips.persistence.specification.SecurityAlertSpecifications.hasSeverity;
import static com.javaguard.ips.persistence.specification.SecurityAlertSpecifications.hasSourceIp;
import static com.javaguard.ips.persistence.specification.SecurityAlertSpecifications.hasType;

import com.javaguard.ips.statistics.SecurityStatisticsSnapshot;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
public class SecurityAlertStore {

    private final SecurityAlertRepository repository;


    public SecurityAlertStore(
            SecurityAlertRepository repository
    ) {

        this.repository = repository;
    }


    @Transactional
    public void save(
            SecurityEvent event
    ) {

        SecurityAlertEntity entity =
                SecurityAlertEntity.from(event);

        repository.save(entity);
    }


    @Transactional(readOnly = true)
    public List<SecurityEvent> findRecent() {

        return repository
                .findTop100ByOrderByDetectedAtDesc()
                .stream()
                .map(SecurityAlertEntity::toDomain)
                .toList();
    }


    @Transactional(readOnly = true)
    public long count() {

        return repository.count();
    }

    @Transactional(readOnly = true)
    public Page<SecurityEvent> search(
            ThreatType type,
            Severity severity,
            String sourceIp,
            String destinationIp,
            int page,
            int size
    ) {

        Specification<SecurityAlertEntity> specification =
                Specification.allOf(
                        hasType(type),
                        hasSeverity(severity),
                        hasSourceIp(sourceIp),
                        hasDestinationIp(destinationIp)
                );

        PageRequest pageRequest =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "detectedAt"
                        )
                );

        return repository
                .findAll(
                        specification,
                        pageRequest
                )
                .map(
                        SecurityAlertEntity::toDomain
                );
    }

    @Transactional(readOnly = true)
    public SecurityStatisticsSnapshot statistics() {

        long totalAlerts =
                repository.count();


        Instant last24Hours =
                Instant.now()
                        .minus(
                                Duration.ofHours(24)
                        );


        long alertsLast24Hours =
                repository.countByDetectedAtAfter(
                        last24Hours
                );


        Map<ThreatType, Long> byThreatType =
                new EnumMap<>(
                        ThreatType.class
                );

        repository
                .countByThreatType()
                .forEach(
                        row ->
                                byThreatType.put(
                                        row.getType(),
                                        row.getCount()
                                )
                );


        Map<Severity, Long> bySeverity =
                new EnumMap<>(
                        Severity.class
                );

        repository
                .countBySeverity()
                .forEach(
                        row ->
                                bySeverity.put(
                                        row.getSeverity(),
                                        row.getCount()
                                )
                );


        List<SecurityStatisticsSnapshot.SourceIpCount>
                topSourceIps =
                repository
                        .findTopSourceIps(
                                PageRequest.of(
                                        0,
                                        5
                                )
                        )
                        .stream()
                        .map(
                                row ->
                                        new SecurityStatisticsSnapshot
                                                .SourceIpCount(
                                                row.getSourceIp(),
                                                row.getCount()
                                        )
                        )
                        .toList();


        return new SecurityStatisticsSnapshot(
                totalAlerts,
                alertsLast24Hours,
                byThreatType,
                bySeverity,
                topSourceIps
        );
    }






}

