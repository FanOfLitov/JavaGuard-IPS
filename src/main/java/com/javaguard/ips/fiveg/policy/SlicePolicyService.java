package com.javaguard.ips.fiveg.policy;

import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.policy.model.SliceSecurityPolicy;
import com.javaguard.ips.persistence.SlicePolicyStore;
import com.javaguard.ips.web.dto.CreateSlicePolicyRequest;

import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlicePolicyService {

    private final SlicePolicyStore store;

    public SlicePolicyService(
            SlicePolicyStore store
    ) {
        this.store = store;
    }


    public SliceSecurityPolicy create(
            CreateSlicePolicyRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Slice policy request must not be null"
            );
        }

        String name =
                requireText(
                        request.name(),
                        "Policy name"
                );

        String dnn =
                requireText(
                        request.dnn(),
                        "DNN"
                )
                        .toLowerCase(Locale.ROOT);

        if (request.maxPacketsPerSecond() < 0) {
            throw new IllegalArgumentException(
                    "Max packets per second must not be negative"
            );
        }

        SliceId slice =
                new SliceId(
                        request.sst(),
                        request.sd()
                );

        if (
                store.exists(
                        slice.sst(),
                        slice.sd(),
                        dnn
                )
        ) {
            throw new IllegalArgumentException(
                    "Security policy already exists for slice "
                            + slice.displayName()
                            + " and DNN "
                            + dnn
            );
        }

        Severity severity =
                request.severity() == null
                        ? Severity.MEDIUM
                        : request.severity();

        SliceSecurityPolicy policy =
                new SliceSecurityPolicy(
                        UUID.randomUUID(),
                        name,
                        slice,
                        dnn,
                        request.allowTcp(),
                        request.allowUdp(),
                        request.allowIcmp(),
                        request.allowOther(),
                        request.maxPacketsPerSecond(),
                        severity,
                        true,
                        Instant.now()
                );

        return store.save(policy);
    }


    public Optional<SliceSecurityPolicy> findMatching(
            PduSessionContext session
    ) {

        if (session == null) {
            return Optional.empty();
        }

        return store.findEnabled(
                session.slice().sst(),
                session.slice().sd(),
                session.dnn()
        );
    }


    public List<SliceSecurityPolicy> findAll() {
        return store.findAll();
    }


    public boolean delete(
            UUID id
    ) {
        return store.delete(id);
    }


    private String requireText(
            String value,
            String field
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be empty"
            );
        }

        return value.trim();
    }
}