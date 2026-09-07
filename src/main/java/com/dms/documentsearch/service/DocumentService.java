package com.dms.documentsearch.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.tika.Tika;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dms.documentsearch.entity.Document;
import com.dms.documentsearch.entity.UserStorage;
import com.dms.documentsearch.repository.DocumentRepository;
import com.dms.documentsearch.repository.UserStorageRepository;

@Service
public class DocumentService {

    // ==========================================
    // REPOSITORIES
    // ==========================================

    private final DocumentRepository documentRepository;

    private final UserStorageRepository userStorageRepository;


    // ==========================================
    // OCR SERVICES
    // ==========================================

    private final OCRService ocrService;

    private final DocumentNumberExtractionService
            documentNumberExtractionService;


    // ==========================================
    // RAG SERVICE
    // ==========================================

    private final DocumentChunkService
            documentChunkService;


    // ==========================================
    // APACHE TIKA
    // ==========================================

    private final Tika tika =
            new Tika();


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DocumentService(
            DocumentRepository documentRepository,
            UserStorageRepository userStorageRepository,
            OCRService ocrService,
            DocumentNumberExtractionService documentNumberExtractionService,
            DocumentChunkService documentChunkService) {

        this.documentRepository =
                documentRepository;

        this.userStorageRepository =
                userStorageRepository;

        this.ocrService =
                ocrService;

        this.documentNumberExtractionService =
                documentNumberExtractionService;

        this.documentChunkService =
                documentChunkService;
    }


    // ==========================================
    // GET USER STORAGE BASE PATH
    // ==========================================

    private Path getBasePath(
            String username) {

        UserStorage userStorage =
                userStorageRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Storage path not configured for user: "
                                                + username
                                )
                        );

        return Paths
                .get(
                        userStorage
                                .getStoragePath()
                )
                .toAbsolutePath()
                .normalize();
    }


    // ==========================================
    // RESOLVE DOCUMENT PHYSICAL PATH
    // ==========================================

    public Path getPhysicalPath(
            Document document) {

        Path storedPath =
                Paths.get(
                        document.getFilePath()
                );

        if (storedPath.isAbsolute()) {

            return storedPath
                    .toAbsolutePath()
                    .normalize();
        }

        Path basePath =
                getBasePath(
                        document
                                .getOwnerUsername()
                );

        Path fullPath =
                basePath
                        .resolve(storedPath)
                        .normalize();

        if (!fullPath.startsWith(basePath)) {

            throw new SecurityException(
                    "Invalid document path"
            );
        }

        return fullPath;
    }


    // ==========================================
    // 1. UPLOAD DOCUMENT
    // ==========================================

    public Document uploadDocument(
            MultipartFile file,
            String username)
            throws IOException {

        // ======================================
        // TOTAL START TIME
        // ======================================

        long totalStartTime =
                System.nanoTime();


        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "DOCUMENT UPLOAD STARTED"
        );
        System.out.println(
                "=========================================="
        );


        // ======================================
        // VALIDATE FILE
        // ======================================

        if (file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a file"
            );
        }


        String originalFileName =
                file.getOriginalFilename();


        if (originalFileName == null ||
                originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }


        // ======================================
        // FILE INFORMATION
        // ======================================

        String safeFileName =
                Paths.get(originalFileName)
                        .getFileName()
                        .toString();


        long fileSize =
                file.getSize();


        System.out.println(
                "File: "
                        + safeFileName
        );

        System.out.println(
                "File size: "
                        + formatFileSize(fileSize)
        );


        // ======================================
        // CURRENT DATE
        // ======================================

        LocalDate currentDate =
                LocalDate.now();


        // ======================================
        // GET USER STORAGE PATH
        // ======================================

        Path basePath =
                getBasePath(
                        username
                );


        // ======================================
        // CREATE DATE FOLDER
        // ======================================

        Path dateFolder =
                basePath
                        .resolve(
                                currentDate
                                        .toString()
                        )
                        .normalize();


        if (!dateFolder.startsWith(basePath)) {

            throw new SecurityException(
                    "Invalid upload path"
            );
        }


        Files.createDirectories(
                dateFolder
        );


        // ======================================
        // CREATE PHYSICAL FILE PATH
        // ======================================

        Path physicalFilePath =
                dateFolder
                        .resolve(
                                safeFileName
                        )
                        .normalize();


        if (!physicalFilePath
                .startsWith(basePath)) {

            throw new SecurityException(
                    "Invalid file path"
            );
        }


        // ======================================
        // 1. SAVE PHYSICAL FILE
        // ======================================

        long fileSaveStartTime =
                System.nanoTime();


        Files.copy(
                file.getInputStream(),
                physicalFilePath,
                StandardCopyOption.REPLACE_EXISTING
        );


        long fileSaveEndTime =
                System.nanoTime();


        long fileSaveTime =
                (fileSaveEndTime -
                        fileSaveStartTime)
                        / 1_000_000;


        System.out.println(
                "File save time: "
                        + fileSaveTime
                        + " ms"
        );


        // ======================================
        // 2. TEXT EXTRACTION / OCR
        // ======================================

        long extractionStartTime =
                System.nanoTime();


        String extractedText = "";


        try {

            String contentType =
                    file.getContentType();


            // ==================================
            // IMAGE
            // ==================================

            if (contentType != null &&
                    (
                            contentType
                                    .equalsIgnoreCase(
                                            "image/jpeg"
                                    )
                            ||
                            contentType
                                    .equalsIgnoreCase(
                                            "image/jpg"
                                    )
                            ||
                            contentType
                                    .equalsIgnoreCase(
                                            "image/png"
                                    )
                    )) {

                System.out.println(
                        "Using OCR for image..."
                );


                extractedText =
                        ocrService
                                .extractText(
                                        physicalFilePath
                                                .toFile()
                                );
            }


            // ==================================
            // PDF
            // ==================================

            else if (contentType != null &&
                    contentType
                            .equalsIgnoreCase(
                                    "application/pdf"
                            )) {

                System.out.println(
                        "Trying Apache Tika for PDF..."
                );


                // ==================================
                // FIRST TRY NORMAL PDF TEXT
                // ==================================

                try (InputStream inputStream =
                             Files.newInputStream(
                                     physicalFilePath
                             )) {

                    extractedText =
                            tika.parseToString(
                                    inputStream
                            );
                }


                // ==================================
                // SCANNED PDF FALLBACK
                // ==================================

                if (extractedText == null ||
                        extractedText
                                .trim()
                                .length() < 20) {

                    System.out.println(
                            "No useful PDF text found. "
                                    + "Switching to OCR..."
                    );


                    // ==============================
                    // GENERAL OCR
                    // ==============================

                    String generalOcrText = "";


                    try {

                        generalOcrText =
                                ocrService
                                        .extractTextFromPdf(
                                                physicalFilePath
                                                        .toFile()
                                        );


                        System.out.println(
                                "General scanned PDF OCR completed."
                        );


                    } catch (Exception e) {

                        System.out.println(
                                "General OCR failed: "
                                        + e.getMessage()
                        );

                        e.printStackTrace();
                    }


                    // ==============================
                    // NUMBER-FOCUSED OCR
                    // ==============================

                    String numberOcrText = "";


                    try {

                        System.out.println(
                                "Starting additional "
                                        + "number-focused OCR..."
                        );


                        numberOcrText =
                                documentNumberExtractionService
                                        .extractNumbersFromPdf(
                                                physicalFilePath
                                                        .toFile()
                                        );


                        System.out.println(
                                "Number-focused OCR completed."
                        );


                    } catch (Exception e) {

                        System.out.println(
                                "Number-focused OCR failed: "
                                        + e.getMessage()
                        );

                        e.printStackTrace();
                    }


                    // ==============================
                    // MERGE OCR RESULTS
                    // ==============================

                    StringBuilder mergedText =
                            new StringBuilder();


                    if (generalOcrText != null &&
                            !generalOcrText
                                    .isBlank()) {

                        mergedText
                                .append(
                                        generalOcrText.trim()
                                );
                    }


                    if (numberOcrText != null &&
                            !numberOcrText
                                    .isBlank()) {

                        if (mergedText.length() > 0) {

                            mergedText
                                    .append(
                                            System.lineSeparator()
                                    )
                                    .append(
                                            System.lineSeparator()
                                    );
                        }


                        mergedText
                                .append(
                                        "----- ADDITIONAL OCR TEXT -----"
                                )
                                .append(
                                        System.lineSeparator()
                                )
                                .append(
                                        numberOcrText.trim()
                                );
                    }


                    extractedText =
                            mergedText
                                    .toString();


                    System.out.println(
                            "OCR results merged successfully."
                    );


                    System.out.println(
                            "Merged OCR characters: "
                                    + extractedText.length()
                    );
                }


                // ==================================
                // NORMAL TEXT PDF
                // ==================================

                else {

                    System.out.println(
                            "Normal PDF text extracted "
                                    + "successfully using Tika."
                    );
                }
            }


            // ==================================
            // DOCX / TXT / XLSX / PPTX ETC.
            // ==================================

            else {

                System.out.println(
                        "Using Apache Tika for document..."
                );


                try (InputStream inputStream =
                             Files.newInputStream(
                                     physicalFilePath
                             )) {

                    extractedText =
                            tika.parseToString(
                                    inputStream
                            );
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Could not extract text: "
                            + e.getMessage()
            );

            e.printStackTrace();

            extractedText = "";
        }


        if (extractedText == null) {

            extractedText = "";
        }


        // ======================================
        // EXTRACTION END TIME
        // ======================================

        long extractionEndTime =
                System.nanoTime();


        long extractionTime =
                (extractionEndTime -
                        extractionStartTime)
                        / 1_000_000;


        System.out.println(
                "Text extraction/OCR time: "
                        + extractionTime
                        + " ms"
        );


        System.out.println(
                "Extracted characters: "
                        + extractedText.length()
        );


        // ======================================
        // CREATE DOCUMENT DATABASE RECORD
        // ======================================

        Document document =
                new Document();


        document.setDocumentName(
                safeFileName
        );


        document.setOriginalFileName(
                safeFileName
        );


        document.setFileType(
                file.getContentType()
        );


        document.setFileSize(
                file.getSize()
        );


        document.setFilePath(
                physicalFilePath
                        .toString()
        );


        document.setUploadedAt(
                LocalDateTime.now()
        );


        document.setExtractedText(
                extractedText
        );


        document.setOwnerUsername(
                username
        );


        // ======================================
        // 3. SAVE DATABASE RECORD
        // ======================================

        long databaseStartTime =
                System.nanoTime();


        Document savedDocument =
                documentRepository
                        .save(document);


        long databaseEndTime =
                System.nanoTime();


        long databaseTime =
                (databaseEndTime -
                        databaseStartTime)
                        / 1_000_000;


        System.out.println(
                "Database save time: "
                        + databaseTime
                        + " ms"
        );


        System.out.println(
                "Document saved with ID: "
                        + savedDocument.getId()
        );


        // ======================================
        // 4. CREATE RAG CHUNKS
        // ======================================

        long ragStartTime =
                System.nanoTime();


        boolean ragCompleted =
                false;


        if (savedDocument
                .getExtractedText() != null &&
                !savedDocument
                        .getExtractedText()
                        .isBlank()) {

            try {

                documentChunkService
                        .createChunks(
                                savedDocument
                        );


                ragCompleted = true;


                System.out.println(
                        "RAG chunks created for document ID: "
                                + savedDocument.getId()
                );


            } catch (Exception e) {

                System.out.println(
                        "Could not create RAG chunks "
                                + "for document ID "
                                + savedDocument.getId()
                                + ": "
                                + e.getMessage()
                );

                e.printStackTrace();
            }


        } else {

            System.out.println(
                    "No extracted text available. "
                            + "RAG chunks were not created "
                            + "for document ID: "
                            + savedDocument.getId()
            );
        }


        long ragEndTime =
                System.nanoTime();


        long ragTime =
                (ragEndTime -
                        ragStartTime)
                        / 1_000_000;


        System.out.println(
                "RAG chunk processing time: "
                        + ragTime
                        + " ms"
        );


        // ======================================
        // TOTAL PROCESSING TIME
        // ======================================

        long totalEndTime =
                System.nanoTime();


        long totalTime =
                (totalEndTime -
                        totalStartTime)
                        / 1_000_000;


        double totalSeconds =
                totalTime / 1000.0;


        // ======================================
        // FINAL TIMING REPORT
        // ======================================

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "DOCUMENT PROCESSING COMPLETED"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "File: "
                        + safeFileName
        );

        System.out.println(
                "File size: "
                        + formatFileSize(fileSize)
        );

        System.out.println(
                "File save time: "
                        + fileSaveTime
                        + " ms"
        );

        System.out.println(
                "Text extraction/OCR time: "
                        + extractionTime
                        + " ms"
        );

        System.out.println(
                "Database save time: "
                        + databaseTime
                        + " ms"
        );

        System.out.println(
                "RAG chunk processing time: "
                        + ragTime
                        + " ms"
        );

        System.out.println(
                "RAG completed: "
                        + ragCompleted
        );

        System.out.println(
                "------------------------------------------"
        );

        System.out.println(
                "TOTAL PROCESSING TIME: "
                        + totalTime
                        + " ms"
        );

        System.out.println(
                "TOTAL PROCESSING TIME: "
                        + String.format(
                                "%.2f",
                                totalSeconds
                        )
                        + " seconds"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println();


        // ======================================
        // RETURN SAVED DOCUMENT
        // ======================================

        return savedDocument;
    }


    // ==========================================
    // FILE SIZE FORMATTER
    // ==========================================

    private String formatFileSize(
            long bytes) {

        if (bytes < 1024) {

            return bytes + " B";
        }


        if (bytes <
                1024 * 1024) {

            return String.format(
                    "%.1f KB",
                    bytes / 1024.0
            );
        }


        if (bytes <
                1024 * 1024 * 1024) {

            return String.format(
                    "%.1f MB",
                    bytes /
                            (1024.0 * 1024.0)
            );
        }


        return String.format(
                "%.1f GB",
                bytes /
                        (1024.0 *
                                1024.0 *
                                1024.0)
        );
    }


    // ==========================================
    // 2. GET DOCUMENTS
    // ==========================================

    public List<Document> getDocuments(
            String username,
            boolean isAdmin) {

        if (isAdmin) {

            return documentRepository
                    .findAll();
        }

        return documentRepository
                .findByOwnerUsername(
                        username
                );
    }


    // ==========================================
    // 3. GET DOCUMENT BY ID
    // ==========================================

    public Document getDocumentById(
            Long id,
            String username,
            boolean isAdmin) {

        Document document =
                documentRepository
                        .findById(id)
                        .orElse(null);


        if (document == null) {

            return null;
        }


        checkPermission(
                document,
                username,
                isAdmin
        );


        return document;
    }


    // ==========================================
    // 4. SEARCH DOCUMENTS
    // ==========================================

    public List<Document> searchDocuments(
            String keyword,
            String username,
            boolean isAdmin) {

        if (isAdmin) {

            return documentRepository
                    .searchAllDocuments(
                            keyword
                    );
        }


        return documentRepository
                .searchUserDocuments(
                        keyword,
                        username
                );
    }


    // ==========================================
    // 5. DELETE DOCUMENT
    // ==========================================

    public boolean deleteDocument(
            Long id,
            String username,
            boolean isAdmin)
            throws IOException {

        Document document =
                documentRepository
                        .findById(id)
                        .orElse(null);


        if (document == null) {

            return false;
        }


        // ======================================
        // CHECK PERMISSION
        // ======================================

        checkPermission(
                document,
                username,
                isAdmin
        );


        // ======================================
        // GET PHYSICAL FILE PATH
        // ======================================

        Path physicalFilePath =
                getPhysicalPath(
                        document
                );


        // ======================================
        // DELETE PHYSICAL FILE
        // ======================================

        Files.deleteIfExists(
                physicalFilePath
        );


        // ======================================
        // DELETE DATABASE RECORD
        // ======================================

        documentRepository
                .delete(document);


        return true;
    }


    // ==========================================
    // PERMISSION CHECK
    // ==========================================

    private void checkPermission(
            Document document,
            String username,
            boolean isAdmin) {

        // ======================================
        // ADMIN CAN ACCESS EVERYTHING
        // ======================================

        if (isAdmin) {

            return;
        }


        // ======================================
        // USER CAN ACCESS ONLY OWN DOCUMENTS
        // ======================================

        if (document.getOwnerUsername() == null ||
                !document
                        .getOwnerUsername()
                        .equals(username)) {

            throw new AccessDeniedException(
                    "You do not have permission "
                            + "to access this document"
            );
        }
    }
}