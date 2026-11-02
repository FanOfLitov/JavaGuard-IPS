package com.javaguard.ips.persistence;

import com.javaguard.ips.fiveg.policy.model.SliceSecurityPolicy;
import com.javaguard.ips.persistence.entity.SliceSecurityPolicyEntity;
import com.javaguard.ips.persistence.repository.SliceSecurityPolicyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlicePolicyStore {

    private final SliceSecurityPolicyRepository repository;

    public SlicePolicyStore(
            SliceSecurityPolicyRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SliceSecurityPolicy save(
            SliceSecurityPolicy policy
    ) {

        return repository
                .save(
                        SliceSecurityPolicyEntity.from(
                                policy
                        )
                )
                .toDomain();
    }


    @Transactional(readOnly = true)
    public Optional<SliceSecurityPolicy> findEnabled(
            int sst,
            String sd,
            String dnn
    ) {

        return repository
                .findFirstBySstAndSdAndDnnIgnoreCaseAndEnabledTrue(
                        sst,
                        normalizeSd(sd),
                        dnn
                )
                .map(
                        SliceSecurityPolicyEntity::toDomain
                );
    }


    @Transactional(readOnly = true)
    public boolean exists(
            int sst,
            String sd,
            String dnn
    ) {

        return repository
                .existsBySstAndSdAndDnnIgnoreCase(
                        sst,
                        normalizeSd(sd),
                        dnn
                );
    }


    @Transactional(readOnly = true)
    public List<SliceSecurityPolicy> findAll() {

        return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(
                        SliceSecurityPolicyEntity::toDomain
                )
                .toList();
    }


    @Transactional
    public boolean delete(
            UUID id
    ) {

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);

        return true;
    }


    private String normalizeSd(
            String sd
    ) {
        return sd == null ? "" : sd;
    }
}