package com.javaguard.ips.persistence;

import com.javaguard.ips.persistence.entity.AllowlistEntryEntity;
import com.javaguard.ips.persistence.repository.AllowlistEntryRepository;

import com.javaguard.ips.response.model.AllowlistEntry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class AllowlistStore {

    private final AllowlistEntryRepository repository;


    public AllowlistStore(
            AllowlistEntryRepository repository
    ) {

        this.repository = repository;
    }


    @Transactional
    public AllowlistEntry save(
            AllowlistEntry entry
    ) {

        AllowlistEntryEntity saved =
                repository.save(
                        AllowlistEntryEntity.from(
                                entry
                        )
                );


        return saved.toDomain();
    }


    @Transactional(readOnly = true)
    public boolean contains(
            String ip
    ) {

        return repository.existsByIp(ip);
    }


    @Transactional(readOnly = true)
    public List<AllowlistEntry> findAll() {

        return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(
                        AllowlistEntryEntity::toDomain
                )
                .toList();
    }


    @Transactional
    public boolean delete(
            String ip
    ) {

        return repository.deleteByIp(ip) > 0;
    }
}