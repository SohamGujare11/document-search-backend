package com.dms.documentsearch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dms.documentsearch.entity.StorageConfig;

public interface StorageConfigRepository
        extends JpaRepository<StorageConfig, Long> {

    Optional<StorageConfig> findByUsername(
            String username
    );

    boolean existsByUsername(
            String username
    );

    void deleteByUsername(
            String username
    );
}