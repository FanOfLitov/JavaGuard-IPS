package com.javaguard.ips.persistence.repository;

import com.javaguard.ips.persistence.entity.AllowlistEntryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface AllowlistEntryRepository
        extends JpaRepository<AllowlistEntryEntity, UUID> {


    Optional<AllowlistEntryEntity>
    findByIp(
            String ip
    );


    boolean existsByIp(
            String ip
    );


    List<AllowlistEntryEntity>
    findAllByOrderByCreatedAtDesc();


    long deleteByIp(
            String ip
    );
}