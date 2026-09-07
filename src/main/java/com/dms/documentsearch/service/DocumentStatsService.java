package com.dms.documentsearch.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dms.documentsearch.entity.Document;
import com.dms.documentsearch.repository.DocumentRepository;

@Service
public class DocumentStatsService {

    private final DocumentRepository documentRepository;


    public DocumentStatsService(
            DocumentRepository documentRepository) {

        this.documentRepository =
                documentRepository;
    }


    // ==========================================
    // DATE HELPERS
    // ==========================================

    private LocalDateTime startOfToday() {

        return LocalDate
                .now()
                .atStartOfDay();
    }


    private LocalDateTime startOfTomorrow() {

        return LocalDate
                .now()
                .plusDays(1)
                .atStartOfDay();
    }


    private LocalDateTime startOfYesterday() {

        return LocalDate
                .now()
                .minusDays(1)
                .atStartOfDay();
    }


    // ==========================================
    // USER - TOTAL
    // ==========================================

    public long getTotalDocuments(
            String username) {

        return documentRepository
                .countByOwnerUsername(username);
    }


    // ==========================================
    // USER - TODAY
    // ==========================================

    public long getDocumentsUploadedToday(
            String username) {

        return documentRepository
                .countByOwnerUsernameAndUploadedAtBetween(
                        username,
                        startOfToday(),
                        startOfTomorrow()
                );
    }


    public List<Document>
    getDocumentsUploadedTodayList(
            String username) {

        return documentRepository
                .findByOwnerUsernameAndUploadedAtBetweenOrderByUploadedAtDesc(
                        username,
                        startOfToday(),
                        startOfTomorrow()
                );
    }


    // ==========================================
    // USER - YESTERDAY
    // ==========================================

    public long getDocumentsUploadedYesterday(
            String username) {

        return documentRepository
                .countByOwnerUsernameAndUploadedAtBetween(
                        username,
                        startOfYesterday(),
                        startOfToday()
                );
    }


    public List<Document>
    getDocumentsUploadedYesterdayList(
            String username) {

        return documentRepository
                .findByOwnerUsernameAndUploadedAtBetweenOrderByUploadedAtDesc(
                        username,
                        startOfYesterday(),
                        startOfToday()
                );
    }


    // ==========================================
    // USER - LATEST DOCUMENT
    // ==========================================

    public Optional<Document>
    getLatestDocument(
            String username) {

        return documentRepository
                .findFirstByOwnerUsernameOrderByUploadedAtDesc(
                        username
                );
    }


    // ==========================================
    // USER - PDF COUNT
    // ==========================================

    public long getPdfCount(
            String username) {

        return documentRepository
                .countByOwnerUsernameAndFileType(
                        username,
                        "application/pdf"
                );
    }


    // ==========================================
    // ADMIN - TOTAL DOCUMENTS
    // ==========================================

    public long getAllDocumentsCount() {

        return documentRepository.count();
    }


    // ==========================================
    // ADMIN - TODAY
    // ==========================================

    public long getAllDocumentsUploadedToday() {

        return documentRepository
                .countByUploadedAtBetween(
                        startOfToday(),
                        startOfTomorrow()
                );
    }


    public List<Document>
    getAllDocumentsUploadedTodayList() {

        return documentRepository
                .findByUploadedAtBetweenOrderByUploadedAtDesc(
                        startOfToday(),
                        startOfTomorrow()
                );
    }


    // ==========================================
    // ADMIN - YESTERDAY
    // ==========================================

    public long getAllDocumentsUploadedYesterday() {

        return documentRepository
                .countByUploadedAtBetween(
                        startOfYesterday(),
                        startOfToday()
                );
    }


    public List<Document>
    getAllDocumentsUploadedYesterdayList() {

        return documentRepository
                .findByUploadedAtBetweenOrderByUploadedAtDesc(
                        startOfYesterday(),
                        startOfToday()
                );
    }


    // ==========================================
    // ADMIN - LATEST DOCUMENT
    // ==========================================

    public Optional<Document>
    getLatestSystemDocument() {

        return documentRepository
                .findFirstByOrderByUploadedAtDesc();
    }


    // ==========================================
    // ADMIN - PDF COUNT
    // ==========================================

    public long getAllPdfCount() {

        return documentRepository
                .countByFileType(
                        "application/pdf"
                );
    }
}