package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.SliceSecurityPolicyEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SliceSecurityPolicyRepository
        extends JpaRepository<SliceSecurityPolicyEntity, UUID> {

    Optional<SliceSecurityPolicyEntity>
    findFirstBySstAndSdAndDnnIgnoreCaseAndEnabledTrue(
            int sst,
            String sd,
            String dnn
    );

    boolean existsBySstAndSdAndDnnIgnoreCase(
            int sst,
            String sd,
            String dnn
    );

    List<SliceSecurityPolicyEntity>
    findAllByOrderByCreatedAtDesc();
}