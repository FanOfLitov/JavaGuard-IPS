package com.javaguard.ips.persistence.entity;

import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.policy.model.SliceSecurityPolicy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "slice_security_policies",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_slice_policy",
                        columnNames = {
                                "sst",
                                "sd",
                                "dnn"
                        }
                )
        }
)
public class SliceSecurityPolicyEntity {

    @Id
    private UUID id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(nullable = false)
    private int sst;

    @Column(
            nullable = false,
            length = 6
    )
    private String sd;

    @Column(
            nullable = false,
            length = 100
    )
    private String dnn;

    @Column(
            name = "allow_tcp",
            nullable = false
    )
    private boolean allowTcp;

    @Column(
            name = "allow_udp",
            nullable = false
    )
    private boolean allowUdp;

    @Column(
            name = "allow_icmp",
            nullable = false
    )
    private boolean allowIcmp;

    @Column(
            name = "allow_other",
            nullable = false
    )
    private boolean allowOther;

    @Column(
            name = "max_packets_per_second",
            nullable = false
    )
    private int maxPacketsPerSecond;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Severity severity;

    @Column(nullable = false)
    private boolean enabled;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected SliceSecurityPolicyEntity() {
    }


    public static SliceSecurityPolicyEntity from(
            SliceSecurityPolicy policy
    ) {

        SliceSecurityPolicyEntity entity =
                new SliceSecurityPolicyEntity();

        entity.id = policy.id();
        entity.name = policy.name();
        entity.sst = policy.slice().sst();

        entity.sd =
                policy.slice().sd() == null
                        ? ""
                        : policy.slice().sd();

        entity.dnn = policy.dnn();

        entity.allowTcp = policy.allowTcp();
        entity.allowUdp = policy.allowUdp();
        entity.allowIcmp = policy.allowIcmp();
        entity.allowOther = policy.allowOther();

        entity.maxPacketsPerSecond =
                policy.maxPacketsPerSecond();

        entity.severity =
                policy.severity();

        entity.enabled =
                policy.enabled();

        entity.createdAt =
                policy.createdAt();

        return entity;
    }


    public SliceSecurityPolicy toDomain() {

        String domainSd =
                sd.isBlank()
                        ? null
                        : sd;

        return new SliceSecurityPolicy(
                id,
                name,
                new SliceId(
                        sst,
                        domainSd
                ),
                dnn,
                allowTcp,
                allowUdp,
                allowIcmp,
                allowOther,
                maxPacketsPerSecond,
                severity,
                enabled,
                createdAt
        );
    }
}