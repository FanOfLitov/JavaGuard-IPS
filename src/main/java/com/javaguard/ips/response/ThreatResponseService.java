package com.javaguard.ips.response;

import com.javaguard.ips.persistence.BlockedIpStore;

import com.javaguard.ips.response.firewall.FirewallAdapter;

import com.javaguard.ips.response.model.BlockRecord;
import com.javaguard.ips.response.model.BlockSource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ThreatResponseService {

    private final FirewallAdapter firewallAdapter;

    private final BlockedIpStore blockedIpStore;
    private final AllowlistService allowlistService;


    private final ProtectedAddressService protectedAddressService;
    public ThreatResponseService(
            FirewallAdapter firewallAdapter,
            BlockedIpStore blockedIpStore,
            ProtectedAddressService protectedAddressService,
            AllowlistService allowlistService
    ) {

        this.firewallAdapter = firewallAdapter;

        this.blockedIpStore = blockedIpStore;

        this.protectedAddressService = protectedAddressService;

        this.allowlistService = allowlistService;
    }

    private static final Logger log =
            LoggerFactory.getLogger(
                    ThreatResponseService.class
            );




    public BlockRecord block(
            String ip,
            Duration duration,
            String reason
    ) {

        return block(
                ip,
                duration,
                reason,
                BlockSource.MANUAL
        );
    }


    public BlockRecord block(
            String ip,
            Duration duration,
            String reason,
            BlockSource source
    ) {

        validateIp(ip);

        if (
                protectedAddressService.isProtected(ip)
        ) {

            throw new IllegalArgumentException(
                    "Protected IP address cannot be blocked: "
                            + ip
            );
        }

        validateDuration(duration);


        Instant now = Instant.now();


        BlockRecord record =
                new BlockRecord(
                        ip.trim(),
                        normalizeReason(reason),
                        now,
                        now.plus(duration)
                );


        boolean alreadyBlocked =
                blockedIpStore.isActive(
                        record.ip()
                );


        if (!alreadyBlocked) {

            firewallAdapter.block(
                    record.ip()
            );
        }

        if (
                allowlistService
                        .isAllowed(ip)
        ) {

            throw new IllegalArgumentException(
                    "Allowlisted IP address cannot be blocked: "
                            + ip
            );
        }


        blockedIpStore.save(
                record,
                source
        );


        return record;
    }


    public synchronized boolean unblock(
            String ip
    ) {

        validateIp(ip);


        if (
                !blockedIpStore.isActive(ip)
        ) {

            return false;
        }


        firewallAdapter.unblock(ip);


        blockedIpStore.deactivate(ip);


        return true;
    }

    public boolean isBlocked(
            String ip
    ) {

        return blockedIpStore.isActive(ip);
    }


    public List<BlockRecord> getBlockedIps() {

        return blockedIpStore.findActive();
    }


    public long getBlockedCount() {

        return blockedIpStore.countActive();
    }


    @Scheduled(
            fixedDelay = 5000
    )
    public void cleanupExpired() {

        Instant now =
                Instant.now();


        List<BlockRecord> expired =
                blockedIpStore.findExpired(now);


        expired.forEach(
                record ->
                        unblock(
                                record.ip()
                        )
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


    public void restoreActiveBlocks() {

        cleanupExpired();


        List<BlockRecord> activeBlocks =
                blockedIpStore.findActive();


        log.info(
                "Restoring {} active firewall blocks",
                activeBlocks.size()
        );


        for (BlockRecord record : activeBlocks) {

            try {

                firewallAdapter.block(
                        record.ip()
                );


                log.info(
                        "Restored firewall block for {}",
                        record.ip()
                );

            } catch (RuntimeException exception) {

                log.error(
                        "Failed to restore firewall block for {}",
                        record.ip(),
                        exception
                );
            }
        }
    }
}