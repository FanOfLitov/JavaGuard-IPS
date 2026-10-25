package com.javaguard.ips.persistence.entity;

import com.javaguard.ips.response.model.BlockRecord;
import com.javaguard.ips.response.model.BlockSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "blocked_ips",
        indexes = {

                @Index(
                        name = "idx_blocked_ips_ip",
                        columnList = "ip"
                ),

                @Index(
                        name = "idx_blocked_ips_active",
                        columnList = "active"
                ),

                @Index(
                        name = "idx_blocked_ips_expires_at",
                        columnList = "expires_at"
                )
        }
)
public class BlockedIpEntity {

    @Id
    private UUID id;


    @Column(
            nullable = false,
            length = 45
    )
    private String ip;


    @Column(
            nullable = false,
            length = 512
    )
    private String reason;


    @Column(
            name = "blocked_at",
            nullable = false
    )
    private Instant blockedAt;


    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;


    @Column(
            nullable = false
    )
    private boolean active;


    @Enumerated(EnumType.STRING)

    @Column(
            nullable = false,
            length = 20
    )
    private BlockSource source;


    protected BlockedIpEntity() {
    }


    private BlockedIpEntity(
            UUID id,
            String ip,
            String reason,
            Instant blockedAt,
            Instant expiresAt,
            boolean active,
            BlockSource source
    ) {

        this.id = id;
        this.ip = ip;
        this.reason = reason;
        this.blockedAt = blockedAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.source = source;
    }


    public static BlockedIpEntity from(
            BlockRecord record,
            BlockSource source
    ) {

        return new BlockedIpEntity(
                UUID.randomUUID(),
                record.ip(),
                record.reason(),
                record.blockedAt(),
                record.expiresAt(),
                true,
                source
        );
    }


    public BlockRecord toDomain() {

        return new BlockRecord(
                ip,
                reason,
                blockedAt,
                expiresAt
        );
    }


    public void deactivate() {

        this.active = false;
    }


    public UUID getId() {
        return id;
    }


    public String getIp() {
        return ip;
    }


    public Instant getExpiresAt() {
        return expiresAt;
    }


    public boolean isActive() {
        return active;
    }


    public BlockSource getSource() {
        return source;
    }
}