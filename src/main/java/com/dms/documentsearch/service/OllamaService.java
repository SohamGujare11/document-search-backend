package com.dms.documentsearch.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OllamaService {

    // ==========================================
    // OLLAMA CONFIGURATION
    // ==========================================

    private final String ollamaUrl =
            "http://localhost:11434/api/chat";

    private final String model =
            "llama3.2:1b";


    // ==========================================
    // REST CLIENT
    // ==========================================

    private final RestClient restClient;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public OllamaService() {

        this.restClient = RestClient.create();
    }


    // ==========================================
    // ASK LOCAL LLM
    // ==========================================

    public String ask(String prompt) {

        try {

            // ==================================
            // REQUEST BODY
            // ==================================

            Map<String, Object> requestBody =
                    Map.of(

                        "model",
                        model,

                        "messages",
                        List.of(
                            Map.of(
                                "role",
                                "user",

                                "content",
                                prompt
                            )
                        ),

                        "stream",
                        false
                    );


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

                return "Ollama returned an empty response.";
            }


            // ==================================
            // GET MESSAGE
            // ==================================

            Object messageObject =
                    response.get("message");


            if (!(messageObject
                    instanceof Map<?, ?> message)) {

                return "Ollama response does not contain a message.";
            }


            // ==================================
            // GET CONTENT
            // ==================================

            Object contentObject =
                    message.get("content");


            if (contentObject == null) {

                return "Ollama returned no answer.";
            }


            // ==================================
            // RETURN ANSWER
            // ==================================

            return contentObject.toString();


        } catch (Exception e) {

            e.printStackTrace();

            return "Ollama Error: "
                    + e.getMessage();
        }
    }
}