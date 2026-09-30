package com.javaguard.ips.persistence;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.persistence.entity.SecurityAlertEntity;
import com.javaguard.ips.persistence.repository.SecurityAlertRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}