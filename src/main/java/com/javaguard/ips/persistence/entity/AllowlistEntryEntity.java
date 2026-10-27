package com.javaguard.ips.persistence.entity;

import com.javaguard.ips.response.model.AllowlistEntry;

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
        name = "allowlist_entries",

        indexes = {@Index(
                        name = "idx_allowlist_ip",
                        columnList = "ip"
                )
        },

        uniqueConstraints = {@UniqueConstraint(
                        name = "uk_allowlist_ip",
                        columnNames = "ip"
                )
        }
)
public class AllowlistEntryEntity {

    @Id
    private UUID id;


    @Column(
            nullable = false,
            length = 45
    )
    private String ip;


    @Column(
            nullable = false,
            length = 255
    )
    private String description;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected AllowlistEntryEntity() {
    }


    private AllowlistEntryEntity(
            UUID id,
            String ip,
            String description,
            Instant createdAt
    ) {

        this.id = id;
        this.ip = ip;
        this.description = description;
        this.createdAt = createdAt;
    }


    public static AllowlistEntryEntity from(
            AllowlistEntry entry
    ) {

        return new AllowlistEntryEntity(
                UUID.randomUUID(),
                entry.ip(),
                entry.description(),
                entry.createdAt()
        );
    }


    public AllowlistEntry toDomain() {

        return new AllowlistEntry(
                ip,
                description,
                createdAt
        );
    }


    public UUID getId() {
        return id;
    }


    public String getIp() {
        return ip;
    }
}