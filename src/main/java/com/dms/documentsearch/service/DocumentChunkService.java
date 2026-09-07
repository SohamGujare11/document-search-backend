package com.dms.documentsearch.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dms.documentsearch.entity.Document;
import com.dms.documentsearch.entity.DocumentChunk;
import com.dms.documentsearch.repository.DocumentChunkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DocumentChunkService {

    // ==========================================
    // DEPENDENCIES
    // ==========================================

    private final DocumentChunkRepository chunkRepository;

    private final EmbeddingService embeddingService;

    private final ObjectMapper objectMapper;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DocumentChunkService(
            DocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService) {

        this.chunkRepository =
                chunkRepository;

        this.embeddingService =
                embeddingService;

        /*
         * Create ObjectMapper directly.
         *
         * This avoids the Spring error:
         *
         * No qualifying bean of type
         * ObjectMapper available
         */

        this.objectMapper =
                new ObjectMapper();
    }


    // ==========================================
    // CREATE DOCUMENT CHUNKS
    // ==========================================

    public List<DocumentChunk> createChunks(
            Document document) {

        // ======================================
        // GET EXTRACTED TEXT
        // ======================================

        String text =
                document.getExtractedText();


        // ======================================
        // VALIDATE TEXT
        // ======================================

        if (text == null ||
                text.isBlank()) {

            System.out.println(
                    "No extracted text found for document ID: "
                            + document.getId()
            );

            return List.of();
        }


        // ======================================
        // CLEAN TEXT
        // ======================================

        text =
                text
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();


        // ======================================
        // DELETE OLD CHUNKS
        // ======================================

        /*
         * If the document is indexed again,
         * remove its previous chunks first.
         */

        chunkRepository
                .deleteByDocumentId(
                        document.getId()
                );


        // ======================================
        // CHUNK SETTINGS
        // ======================================

        /*
         * Each chunk contains approximately
         * 1000 characters.
         */

        int chunkSize =
                1000;


        /*
         * Keep 150 characters from the
         * previous chunk.
         *
         * This helps preserve sentences or
         * information located near chunk
         * boundaries.
         */

        int overlap =
                150;


        // ======================================
        // CHUNK LIST
        // ======================================

        List<DocumentChunk> chunks =
                new ArrayList<>();


        int start =
                0;


        int chunkIndex =
                0;


        // ======================================
        // CREATE CHUNKS
        // ======================================

        while (start < text.length()) {

            // ==================================
            // CALCULATE END POSITION
            // ==================================

            int end =
                    Math.min(
                            start + chunkSize,
                            text.length()
                    );


            // ==================================
            // GET CHUNK TEXT
            // ==================================

            String chunkText =
                    text
                            .substring(
                                    start,
                                    end
                            )
                            .trim();


            // ==================================
            // CREATE CHUNK OBJECT
            // ==================================

            DocumentChunk chunk =
                    new DocumentChunk(
                            document.getId(),
                            document.getOwnerUsername(),
                            chunkIndex,
                            chunkText
                    );


            // ==================================
            // GENERATE EMBEDDING
            // ==================================

            try {

                System.out.println(
                        "Generating embedding for document "
                                + document.getId()
                                + ", chunk "
                                + chunkIndex
                );


                // ==================================
                // CALL GEMINI EMBEDDING API
                // ==================================

                List<Double> embedding =
                        embeddingService
                                .generateEmbedding(
                                        chunkText
                                );


                // ==================================
                // CONVERT VECTOR TO JSON
                // ==================================

                /*
                 * Example:
                 *
                 * [
                 *   0.0234,
                 *   -0.4532,
                 *   0.1234,
                 *   ...
                 * ]
                 */

                String embeddingJson =
                        objectMapper
                                .writeValueAsString(
                                        embedding
                                );


                // ==================================
                // STORE EMBEDDING
                // ==================================

                chunk.setEmbedding(
                        embeddingJson
                );


                System.out.println(
                        "Embedding generated successfully. "
                                + "Dimensions: "
                                + embedding.size()
                );


            }

            // ======================================
            // JSON ERROR
            // ======================================

            catch (JsonProcessingException e) {

                System.out.println(
                        "Could not convert embedding "
                                + "to JSON for chunk "
                                + chunkIndex
                );


                throw new RuntimeException(
                        "Could not convert embedding to JSON",
                        e
                );
            }


            // ======================================
            // EMBEDDING API ERROR
            // ======================================

            catch (Exception e) {

                System.out.println(
                        "Embedding generation failed "
                                + "for document "
                                + document.getId()
                                + ", chunk "
                                + chunkIndex
                                + ": "
                                + e.getMessage()
                );


                throw new RuntimeException(
                        "Could not generate embedding",
                        e
                );
            }


            // ==================================
            // ADD CHUNK TO LIST
            // ==================================

            chunks.add(
                    chunk
            );


            // ==================================
            // CHECK IF LAST CHUNK
            // ==================================

            if (end ==
                    text.length()) {

                break;
            }


            // ==================================
            // MOVE TO NEXT CHUNK
            // ==================================

            start =
                    end - overlap;


            chunkIndex++;
        }


        // ======================================
        // SAVE ALL CHUNKS
        // ======================================

        List<DocumentChunk> savedChunks =
                chunkRepository
                        .saveAll(
                                chunks
                        );


        // ======================================
        // SUCCESS MESSAGE
        // ======================================

        System.out.println(
                "Saved "
                        + savedChunks.size()
                        + " RAG chunks for document ID: "
                        + document.getId()
        );


        // ======================================
        // RETURN SAVED CHUNKS
        // ======================================

        return savedChunks;
    }
}