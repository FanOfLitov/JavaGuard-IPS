package com.javaguard.ips.persistence;

import com.javaguard.ips.persistence.entity.BlockedIpEntity;
import com.javaguard.ips.persistence.repository.BlockedIpRepository;

import com.javaguard.ips.response.model.BlockRecord;
import com.javaguard.ips.response.model.BlockSource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.util.List;


@Service
public class BlockedIpStore {

    private final BlockedIpRepository repository;


    public BlockedIpStore(
            BlockedIpRepository repository
    ) {

        this.repository = repository;
    }


    @Transactional
    public void save(
            BlockRecord record,
            BlockSource source
    ) {

        /*
         * Старую активную блокировку этого IP
         * переводим в history.
         */
        repository
                .findByIpAndActiveTrue(
                        record.ip()
                )
                .forEach(
                        BlockedIpEntity::deactivate
                );


        repository.save(
                BlockedIpEntity.from(
                        record,
                        source
                )
        );
    }


    @Transactional
    public boolean deactivate(
            String ip
    ) {

        List<BlockedIpEntity> active =
                repository
                        .findByIpAndActiveTrue(ip);


        if (active.isEmpty()) {
            return false;
        }


        active.forEach(
                BlockedIpEntity::deactivate
        );


        return true;
    }


    @Transactional(readOnly = true)
    public boolean isActive(
            String ip
    ) {

        return repository
                .findFirstByIpAndActiveTrue(ip)
                .isPresent();
    }


    @Transactional(readOnly = true)
    public List<BlockRecord> findActive() {

        return repository
                .findByActiveTrueOrderByBlockedAtDesc()
                .stream()
                .map(
                        BlockedIpEntity::toDomain
                )
                .toList();
    }


    @Transactional(readOnly = true)
    public List<BlockRecord> findExpired(
            Instant now
    ) {

        return repository
                .findByActiveTrueAndExpiresAtLessThanEqual(
                        now
                )
                .stream()
                .map(
                        BlockedIpEntity::toDomain
                )
                .toList();
    }


    @Transactional(readOnly = true)
    public long countActive() {

        return repository
                .countByActiveTrue();
    }
}