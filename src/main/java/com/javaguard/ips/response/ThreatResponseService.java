package com.javaguard.ips.response;

import com.javaguard.ips.response.firewall.FirewallAdapter;
import com.javaguard.ips.response.model.BlockRecord;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;


@Service
public class ThreatResponseService {

    private final FirewallAdapter firewallAdapter;


    private final Map<String, BlockRecord> blockedIps =
            new ConcurrentHashMap<>();


    public ThreatResponseService(
            FirewallAdapter firewallAdapter
    ) {

        this.firewallAdapter =
                firewallAdapter;
    }


    public BlockRecord block(
            String ip,
            Duration duration,
            String reason
    ) {

        validateIp(ip);
        validateDuration(duration);


        Instant now =
                Instant.now();


        Instant expiresAt =
                now.plus(duration);


        String normalizedReason =
                normalizeReason(reason);


        BlockRecord record =
                new BlockRecord(
                        ip,
                        normalizedReason,
                        now,
                        expiresAt
                );


        firewallAdapter.block(ip);


        blockedIps.put(
                ip,
                record
        );


        return record;
    }


    public boolean unblock(
            String ip
    ) {

        BlockRecord removed =
                blockedIps.remove(ip);


        if (removed == null) {
            return false;
        }


        firewallAdapter.unblock(ip);


        return true;
    }


    public boolean isBlocked(
            String ip
    ) {

        cleanupExpired();


        return blockedIps.containsKey(ip);
    }


    public List<BlockRecord> getBlockedIps() {

        cleanupExpired();


        return blockedIps
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        BlockRecord::blockedAt
                                )
                                .reversed()
                )
                .toList();
    }


    public long getBlockedCount() {

        cleanupExpired();


        return blockedIps.size();
    }


    @Scheduled(
            fixedDelay = 5000
    )
    public void cleanupExpired() {

        Instant now =
                Instant.now();


        List<String> expiredIps =
                blockedIps
                        .values()
                        .stream()
                        .filter(
                                record ->
                                        !record
                                                .expiresAt()
                                                .isAfter(now)
                        )
                        .map(
                                BlockRecord::ip
                        )
                        .toList();


        expiredIps.forEach(
                this::unblock
        );
    }


    private void validateIp(
            String ip
    ) {

        if (
                ip == null
                        ||
                        ip.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "IP address must not be empty"
            );
        }
    }


    private void validateDuration(
            Duration duration
    ) {

        if (
                duration == null
                        ||
                        duration.isZero()
                        ||
                        duration.isNegative()
        ) {

            throw new IllegalArgumentException(
                    "Block duration must be positive"
            );
        }
    }


    private String normalizeReason(
            String reason
    ) {

        if (
                reason == null
                        ||
                        reason.isBlank()
        ) {

            return "Manual block";
        }


        return reason.trim();
    }
}