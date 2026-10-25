package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.BlockedIpEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface BlockedIpRepository
        extends JpaRepository<BlockedIpEntity, UUID> {


    List<BlockedIpEntity>
    findByActiveTrueOrderByBlockedAtDesc();


    List<BlockedIpEntity>
    findByIpAndActiveTrue(
            String ip
    );


    Optional<BlockedIpEntity>
    findFirstByIpAndActiveTrue(
            String ip
    );


    List<BlockedIpEntity>
    findByActiveTrueAndExpiresAtLessThanEqual(
            Instant timestamp
    );


    long countByActiveTrue();
}