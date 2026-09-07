package com.dms.documentsearch.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dms.documentsearch.entity.Document;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {


    // ==========================================
    // EXISTING METHODS
    // ==========================================

    List<Document> findByOwnerUsername(
            String ownerUsername
    );


    @Query(
        value = """
            SELECT *
            FROM documents
            WHERE LOWER(document_name)
                  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(extracted_text)
                  LIKE LOWER(CONCAT('%', :keyword, '%'))
            """,
        nativeQuery = true
    )
    List<Document> searchAllDocuments(
            @Param("keyword") String keyword
    );


    @Query(
        value = """
            SELECT *
            FROM documents
            WHERE owner_username = :username
              AND (
                    LOWER(document_name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR
                    LOWER(extracted_text)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """,
        nativeQuery = true
    )
    List<Document> searchUserDocuments(
            @Param("keyword") String keyword,
            @Param("username") String username
    );


    // ==========================================
    // USER DOCUMENT STATISTICS
    // ==========================================

    long countByOwnerUsername(
            String ownerUsername
    );


    long countByOwnerUsernameAndUploadedAtBetween(
            String ownerUsername,
            LocalDateTime start,
            LocalDateTime end
    );


    List<Document>
    findByOwnerUsernameAndUploadedAtBetweenOrderByUploadedAtDesc(
            String ownerUsername,
            LocalDateTime start,
            LocalDateTime end
    );


    Optional<Document>
    findFirstByOwnerUsernameOrderByUploadedAtDesc(
            String ownerUsername
    );


    long countByOwnerUsernameAndFileType(
            String ownerUsername,
            String fileType
    );


    // ==========================================
    // ADMIN / SYSTEM-WIDE STATISTICS
    // ==========================================

    long countByUploadedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );


    List<Document>
    findByUploadedAtBetweenOrderByUploadedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );


    Optional<Document>
    findFirstByOrderByUploadedAtDesc();


    long countByFileType(
            String fileType
    );
}