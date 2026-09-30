package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.SecurityAlertEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlertEntity, UUID> {

    List<SecurityAlertEntity>
    findTop100ByOrderByDetectedAtDesc();
}