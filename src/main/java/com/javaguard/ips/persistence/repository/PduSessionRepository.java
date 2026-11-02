package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.PduSessionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PduSessionRepository
        extends JpaRepository<PduSessionEntity, UUID> {

    Optional<PduSessionEntity>
    findBySupiAndPduSessionId(
            String supi,
            int pduSessionId
    );

    List<PduSessionEntity>
    findAllByOrderByUpdatedAtDesc();
}