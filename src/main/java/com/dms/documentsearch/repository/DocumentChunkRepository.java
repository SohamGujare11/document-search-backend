package com.dms.documentsearch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dms.documentsearch.entity.DocumentChunk;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(
            Long documentId
    );

    List<DocumentChunk> findByOwnerUsername(
            String ownerUsername
    );

    void deleteByDocumentId(
            Long documentId
    );
}