package com.javaguard.ips.persistence.entity;

import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.model.SliceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "pdu_sessions",

        indexes = {
                @Index(
                        name = "idx_pdu_session_supi",
                        columnList = "supi"
                ),

                @Index(
                        name = "idx_pdu_session_ue_ip",
                        columnList = "ue_ip"
                ),

                @Index(
                        name = "idx_pdu_session_dnn",
                        columnList = "dnn"
                )
        },

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pdu_session_identity",
                        columnNames = {
                                "supi",
                                "pdu_session_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_pdu_session_ue_ip",
                        columnNames = "ue_ip"
                ),

                @UniqueConstraint(
                        name = "uk_pdu_uplink_tunnel",
                        columnNames = {
                                "upf_ip",
                                "uplink_teid"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_pdu_downlink_tunnel",
                        columnNames = {
                                "gnb_ip",
                                "downlink_teid"
                        }
                )
        }
)
public class PduSessionEntity {

    @Id
    private UUID id;

    @Column(
            nullable = false,
            length = 64
    )
    private String supi;

    @Column(
            name = "pdu_session_id",
            nullable = false
    )
    private int pduSessionId;

    @Column(
            name = "ue_ip",
            nullable = false,
            length = 45
    )
    private String ueIp;

    @Column(
            nullable = false,
            length = 100
    )
    private String dnn;

    @Column(
            nullable = false
    )
    private int sst;

    @Column(
            nullable = false,
            length = 6
    )
    private String sd;

    @Column(
            name = "gnb_ip",
            nullable = false,
            length = 45
    )
    private String gnbIp;

    @Column(
            name = "upf_ip",
            nullable = false,
            length = 45
    )
    private String upfIp;

    @Column(
            name = "uplink_teid",
            nullable = false
    )
    private long uplinkTeid;

    @Column(
            name = "downlink_teid",
            nullable = false
    )
    private long downlinkTeid;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    protected PduSessionEntity() {
    }


    private PduSessionEntity(
            UUID id,
            String supi,
            int pduSessionId,
            String ueIp,
            String dnn,
            int sst,
            String sd,
            String gnbIp,
            String upfIp,
            long uplinkTeid,
            long downlinkTeid,
            Instant updatedAt
    ) {
        this.id = id;
        this.supi = supi;
        this.pduSessionId = pduSessionId;
        this.ueIp = ueIp;
        this.dnn = dnn;
        this.sst = sst;
        this.sd = sd;
        this.gnbIp = gnbIp;
        this.upfIp = upfIp;
        this.uplinkTeid = uplinkTeid;
        this.downlinkTeid = downlinkTeid;
        this.updatedAt = updatedAt;
    }


    public static PduSessionEntity from(
            PduSessionContext context
    ) {

        String sd =
                context.slice().sd() == null
                        ? ""
                        : context.slice().sd();

        return new PduSessionEntity(
                context.id(),
                context.supi(),
                context.pduSessionId(),
                context.ueIp(),
                context.dnn(),
                context.slice().sst(),
                sd,
                context.gnbIp(),
                context.upfIp(),
                context.uplinkTeid(),
                context.downlinkTeid(),
                context.updatedAt()
        );
    }


    public PduSessionContext toDomain() {

        String domainSd =
                sd.isBlank()
                        ? null
                        : sd;

        return new PduSessionContext(
                id,
                supi,
                pduSessionId,
                ueIp,
                dnn,
                new SliceId(
                        sst,
                        domainSd
                ),
                gnbIp,
                upfIp,
                uplinkTeid,
                downlinkTeid,
                updatedAt
        );
    }


    public UUID getId() {
        return id;
    }
}