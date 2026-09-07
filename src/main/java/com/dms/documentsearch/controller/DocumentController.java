package com.dms.documentsearch.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dms.documentsearch.entity.Document;
import com.dms.documentsearch.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class DocumentController {

    private final DocumentService documentService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DocumentController(
            DocumentService documentService) {

        this.documentService =
                documentService;
    }


    // ==========================================
    // HELPER - CHECK ADMIN ROLE
    // ==========================================

    private boolean isAdmin(
            Authentication authentication) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                    authority ->
                        authority
                            .getAuthority()
                            .equals("ROLE_ADMIN")
                );
    }


    // ==========================================
    // 1. UPLOAD DOCUMENT
    // ==========================================

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>>
    uploadDocument(
            @RequestParam("file")
            MultipartFile file,

            Authentication authentication)
            throws IOException {


        // ======================================
        // START TOTAL UPLOAD TIMER
        // ======================================

        long startTime =
                System.nanoTime();


        // ======================================
        // GET LOGGED-IN USERNAME
        // ======================================

        String username =
                authentication.getName();


        // ======================================
        // UPLOAD + PROCESS DOCUMENT
        // ======================================

        Document document =
                documentService
                        .uploadDocument(
                                file,
                                username
                        );


        // ======================================
        // END TOTAL UPLOAD TIMER
        // ======================================

        long endTime =
                System.nanoTime();


        // ======================================
        // CALCULATE TOTAL TIME
        // ======================================

        long uploadTimeMs =
                (endTime - startTime)
                        / 1_000_000;


        double uploadTimeSeconds =
                uploadTimeMs / 1000.0;


        // ======================================
        // PRINT TIME IN ECLIPSE CONSOLE
        // ======================================

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "DOCUMENT UPLOAD COMPLETED"
        );

        System.out.println(
                "Document ID: "
                        + document.getId()
        );

        System.out.println(
                "File Name: "
                        + document.getOriginalFileName()
        );

        System.out.println(
                "Total Upload Processing Time: "
                        + uploadTimeMs
                        + " ms"
        );

        System.out.println(
                "Total Upload Processing Time: "
                        + uploadTimeSeconds
                        + " seconds"
        );

        System.out.println(
                "=========================================="
        );


        // ======================================
        // CREATE RESPONSE
        // ======================================

        Map<String, Object> response =
                Map.of(
                        "document",
                        document,

                        "uploadTimeMs",
                        uploadTimeMs,

                        "uploadTimeSeconds",
                        uploadTimeSeconds
                );


        // ======================================
        // RETURN RESPONSE TO FRONTEND
        // ======================================

        return ResponseEntity.ok(
                response
        );
    }


    // ==========================================
    // 2. GET DOCUMENTS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Document>>
    getDocuments(
            Authentication authentication) {

        // Get logged-in username
        String username =
                authentication.getName();


        // Get documents according to role
        List<Document> documents =
                documentService
                        .getDocuments(
                                username,
                                isAdmin(authentication)
                        );


        return ResponseEntity.ok(
                documents
        );
    }


    // ==========================================
    // 3. SEARCH DOCUMENTS
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<List<Document>>
    searchDocuments(
            @RequestParam String keyword,

            Authentication authentication) {

        // Get logged-in username
        String username =
                authentication.getName();


        // Search documents
        List<Document> results =
                documentService
                        .searchDocuments(
                                keyword,
                                username,
                                isAdmin(authentication)
                        );


        return ResponseEntity.ok(
                results
        );
    }


    // ==========================================
    // 4. VIEW DOCUMENT
    // ==========================================

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource>
    viewDocument(
            @PathVariable Long id,

            Authentication authentication)
            throws IOException {

        // Get document and check permission
        Document document =
                documentService
                        .getDocumentById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication)
                        );


        // Document not found
        if (document == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        /*
         * Get actual physical file path.
         *
         * For new documents:
         * base path + relative path
         *
         * For old documents:
         * existing absolute path
         */

        Path filePath =
                documentService
                        .getPhysicalPath(
                                document
                        );


        // Create resource
        Resource resource =
                new UrlResource(
                        filePath.toUri()
                );


        // Physical file not found
        if (!resource.exists() ||
                !resource.isReadable()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // Display file in browser/frontend
        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.CONTENT_TYPE,
                        document.getFileType()
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,

                        "inline; filename=\""
                        + document
                            .getOriginalFileName()
                        + "\""
                )

                .body(resource);
    }


    // ==========================================
    // 5. DOWNLOAD DOCUMENT
    // ==========================================

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource>
    downloadDocument(
            @PathVariable Long id,

            Authentication authentication)
            throws IOException {

        // Get document and check permission
        Document document =
                documentService
                        .getDocumentById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication)
                        );


        // Document not found
        if (document == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        /*
         * Build the actual physical path
         * using storage_config when needed.
         */

        Path filePath =
                documentService
                        .getPhysicalPath(
                                document
                        );


        // Create resource
        Resource resource =
                new UrlResource(
                        filePath.toUri()
                );


        // Physical file not found
        if (!resource.exists() ||
                !resource.isReadable()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // Download file
        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "application/octet-stream"
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=\""
                        + document
                            .getOriginalFileName()
                        + "\""
                )

                .body(resource);
    }


    // ==========================================
    // 6. DELETE DOCUMENT
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteDocument(
            @PathVariable Long id,

            Authentication authentication)
            throws IOException {

        // Delete document
        boolean deleted =
                documentService
                        .deleteDocument(
                                id,
                                authentication.getName(),
                                isAdmin(authentication)
                        );


        // Document not found
        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity.ok(
                "Document deleted successfully"
        );
    }
}