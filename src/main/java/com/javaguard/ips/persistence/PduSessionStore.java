package com.javaguard.ips.persistence;

import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.persistence.entity.PduSessionEntity;
import com.javaguard.ips.persistence.repository.PduSessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PduSessionStore {

    private final PduSessionRepository repository;

    public PduSessionStore(
            PduSessionRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public PduSessionContext save(
            PduSessionContext context
    ) {

        repository
                .findBySupiAndPduSessionId(
                        context.supi(),
                        context.pduSessionId()
                )
                .ifPresent(
                        existing -> {
                            if (!existing.getId().equals(context.id())) {
                                repository.delete(existing);
                                repository.flush();
                            }
                        }
                );

        return repository
                .save(
                        PduSessionEntity.from(context)
                )
                .toDomain();
    }


    @Transactional(readOnly = true)
    public List<PduSessionContext> findAll() {

        return repository
                .findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(PduSessionEntity::toDomain)
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
}