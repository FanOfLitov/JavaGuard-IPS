package com.javaguard.ips.persistence.entity;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

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
        name = "security_alerts",
        indexes = {
                @Index(
                        name = "idx_security_alerts_detected_at",
                        columnList = "detected_at"
                ),
                @Index(
                        name = "idx_security_alerts_source_ip",
                        columnList = "source_ip"
                )
        }
)
public class SecurityAlertEntity {

    @Id
    @Column(
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "detected_at",
            nullable = false
    )
    private Instant detectedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "threat_type",
            nullable = false,
            length = 32
    )
    private ThreatType type;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 16
    )
    private Severity severity;

    @Column(
            name = "source_ip",
            nullable = false,
            length = 45
    )
    private String sourceIp;

    @Column(
            name = "destination_ip",
            nullable = false,
            length = 45
    )
    private String destinationIp;

    @Column(
            nullable = false,
            length = 1024
    )
    private String description;

    @Column(
            name = "evidence_count",
            nullable = false
    )
    private int evidenceCount;


    protected SecurityAlertEntity() {
    }


    private SecurityAlertEntity(
            UUID id,
            Instant detectedAt,
            ThreatType type,
            Severity severity,
            String sourceIp,
            String destinationIp,
            String description,
            int evidenceCount
    ) {

        this.id = id;
        this.detectedAt = detectedAt;
        this.type = type;
        this.severity = severity;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.description = description;
        this.evidenceCount = evidenceCount;
    }


    public static SecurityAlertEntity from(
            SecurityEvent event
    ) {

        return new SecurityAlertEntity(
                event.id(),
                event.timestamp(),
                event.type(),
                event.severity(),
                event.sourceIp(),
                event.destinationIp(),
                event.description(),
                event.evidenceCount()
        );
    }


    public SecurityEvent toDomain() {

        return new SecurityEvent(
                id,
                detectedAt,
                type,
                severity,
                sourceIp,
                destinationIp,
                description,
                evidenceCount
        );
    }


    public UUID getId() {
        return id;
    }


    public Instant getDetectedAt() {
        return detectedAt;
    }


    public ThreatType getType() {
        return type;
    }


    public Severity getSeverity() {
        return severity;
    }


    public String getSourceIp() {
        return sourceIp;
    }


    public String getDestinationIp() {
        return destinationIp;
    }


    public String getDescription() {
        return description;
    }


    public int getEvidenceCount() {
        return evidenceCount;
    }
}