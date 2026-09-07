package com.dms.documentsearch.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmbeddingService {

    // ==========================================
    // OLLAMA CONFIGURATION
    // ==========================================

    private final String ollamaUrl =
            "http://localhost:11434/api/embed";

    private final String model =
            "nomic-embed-text";


    // ==========================================
    // REST CLIENT
    // ==========================================

    private final RestClient restClient;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EmbeddingService() {

        this.restClient =
                RestClient.create();
    }


    // ==========================================
    // GENERATE EMBEDDING
    // ==========================================

    public List<Double> generateEmbedding(
            String text) {

        // ======================================
        // VALIDATE TEXT
        // ======================================

        if (text == null ||
                text.isBlank()) {

            throw new IllegalArgumentException(
                    "Text cannot be empty"
            );
        }


        // ======================================
        // REQUEST BODY
        // ======================================

        Map<String, Object> requestBody =
                Map.of(

                    "model",
                    model,

                    "input",
                    text
                );


        try {

            // ==================================
            // CALL OLLAMA
            // ==================================

            Map<?, ?> response =
                    restClient
                            .post()

                            .uri(ollamaUrl)

                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON_VALUE
                            )

                            .body(requestBody)

                            .retrieve()

                            .body(Map.class);


            // ==================================
            // CHECK RESPONSE
            // ==================================

            if (response == null) {

                throw new RuntimeException(
                        "Ollama returned an empty response"
                );
            }


            // ==================================
            // GET EMBEDDINGS
            // ==================================

            Object embeddingsObject =
                    response.get("embeddings");


            if (!(embeddingsObject
                    instanceof List<?> embeddings)
                    || embeddings.isEmpty()) {

                throw new RuntimeException(
                        "Embeddings not found in Ollama response"
                );
            }


            // ==================================
            // GET FIRST EMBEDDING
            // ==================================

            Object firstEmbeddingObject =
                    embeddings.get(0);


            if (!(firstEmbeddingObject
                    instanceof List<?> rawValues)) {

                throw new RuntimeException(
                        "Invalid embedding returned by Ollama"
                );
            }


            // ==================================
            // CONVERT TO DOUBLE
            // ==================================

            List<Double> embedding =
                    rawValues
                            .stream()
                            .map(value ->
                                    ((Number) value)
                                            .doubleValue()
                            )
                            .toList();


            // ==================================
            // LOG DIMENSION
            // ==================================

            System.out.println(
                    "Ollama embedding generated. "
                    + "Dimensions: "
                    + embedding.size()
            );


            return embedding;


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Could not generate local embedding: "
                            + e.getMessage(),
                    e
            );
        }
    }
}