package com.dms.documentsearch.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dms.documentsearch.entity.DocumentChunk;
import com.dms.documentsearch.repository.DocumentChunkRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RagRetrievalService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RagRetrievalService(
            DocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService) {

        this.chunkRepository =
                chunkRepository;

        this.embeddingService =
                embeddingService;
    }


    // ==========================================
    // FIND RELEVANT CHUNKS
    // ==========================================

    public List<DocumentChunk> findRelevantChunks(
            String question,
            String username) {

        if (question == null ||
                question.isBlank()) {

            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }


        // ======================================
        // CREATE QUESTION EMBEDDING
        // ======================================

        System.out.println(
                "Generating embedding for question..."
        );


        List<Double> questionEmbedding =
                embeddingService
                        .generateEmbedding(
                                question
                        );


        System.out.println(
                "Question embedding generated. Dimensions: "
                        + questionEmbedding.size()
        );


        // ======================================
        // GET ONLY THIS USER'S CHUNKS
        // ======================================

        List<DocumentChunk> userChunks =
                chunkRepository
                        .findByOwnerUsername(
                                username
                        );


        System.out.println(
                "Found "
                        + userChunks.size()
                        + " chunks for user: "
                        + username
        );


        // ======================================
        // CALCULATE SIMILARITIES
        // ======================================

        List<ScoredChunk> scoredChunks =
                new ArrayList<>();


        for (DocumentChunk chunk : userChunks) {

            // Old chunks may not have embeddings.

            if (chunk.getEmbedding() == null ||
                    chunk.getEmbedding().isBlank()) {

                continue;
            }


            try {

                // ==================================
                // JSON -> VECTOR
                // ==================================

                List<Double> chunkEmbedding =
                        objectMapper.readValue(
                                chunk.getEmbedding(),
                                new TypeReference<List<Double>>() {}
                        );


                // ==================================
                // DIMENSION CHECK
                // ==================================

                if (chunkEmbedding.size()
                        != questionEmbedding.size()) {

                    System.out.println(
                            "Skipping chunk "
                                    + chunk.getId()
                                    + " because embedding dimensions differ."
                    );

                    continue;
                }


                // ==================================
                // COSINE SIMILARITY
                // ==================================

                double similarity =
                        cosineSimilarity(
                                questionEmbedding,
                                chunkEmbedding
                        );


                scoredChunks.add(
                        new ScoredChunk(
                                chunk,
                                similarity
                        )
                );


                System.out.println(
                        "Chunk "
                                + chunk.getId()
                                + " similarity = "
                                + similarity
                );


            } catch (Exception e) {

                System.out.println(
                        "Could not read embedding for chunk "
                                + chunk.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }


        // ======================================
        // SORT HIGHEST SIMILARITY FIRST
        // ======================================

        scoredChunks.sort(
                Comparator
                        .comparingDouble(
                                ScoredChunk::score
                        )
                        .reversed()
        );


        // ======================================
        // RETURN TOP 3 CHUNKS
        // ======================================

        return scoredChunks
                .stream()
                .limit(3)
                .map(ScoredChunk::chunk)
                .toList();
    }


    // ==========================================
    // COSINE SIMILARITY
    // ==========================================

    private double cosineSimilarity(
            List<Double> vectorA,
            List<Double> vectorB) {

        if (vectorA.size() != vectorB.size()) {

            throw new IllegalArgumentException(
                    "Embedding dimensions must match"
            );
        }


        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;


        for (int i = 0;
             i < vectorA.size();
             i++) {

            double a =
                    vectorA.get(i);

            double b =
                    vectorB.get(i);


            dotProduct +=
                    a * b;


            magnitudeA +=
                    a * a;


            magnitudeB +=
                    b * b;
        }


        if (magnitudeA == 0.0 ||
                magnitudeB == 0.0) {

            return 0.0;
        }


        return dotProduct /
                (
                    Math.sqrt(magnitudeA)
                    *
                    Math.sqrt(magnitudeB)
                );
    }


    // ==========================================
    // INTERNAL SCORED CHUNK
    // ==========================================

    private record ScoredChunk(
            DocumentChunk chunk,
            double score) {
    }
}