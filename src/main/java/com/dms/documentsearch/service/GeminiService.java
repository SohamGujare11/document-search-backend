package com.dms.documentsearch.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiService {

    // ==========================================
    // GEMINI CONFIGURATION
    // ==========================================

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiUrl;

    private final String model = "gemini-3.6-flash";


    // ==========================================
    // REST CLIENT
    // ==========================================

    private final RestClient restClient;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public GeminiService() {

        this.restClient = RestClient.create();
    }


    // ==========================================
    // ASK GEMINI
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

                            "input",
                            prompt
                    );


            // ==================================
            // CALL GEMINI API
            // ==================================

            Map<?, ?> response =
                    restClient
                            .post()

                            .uri(
                                    geminiUrl
                                            + "?key="
                                            + apiKey
                            )

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

                return "Gemini returned an empty response.";
            }


            // ==================================
            // GET STEPS
            // ==================================

            Object stepsObject =
                    response.get("steps");


            if (!(stepsObject instanceof List<?> steps)) {

                return "Gemini response does not contain model output.";
            }


            // ==================================
            // FIND MODEL OUTPUT
            // ==================================

            for (Object stepObject : steps) {

                if (!(stepObject instanceof Map<?, ?> step)) {

                    continue;
                }


                Object typeObject =
                        step.get("type");


                if (!"model_output".equals(
                        String.valueOf(typeObject))) {

                    continue;
                }


                // ==================================
                // GET CONTENT
                // ==================================

                Object contentObject =
                        step.get("content");


                if (!(contentObject instanceof List<?> contentList)) {

                    continue;
                }


                // ==================================
                // GET TEXT
                // ==================================

                for (Object contentObjectItem :
                        contentList) {

                    if (!(contentObjectItem
                            instanceof Map<?, ?> content)) {

                        continue;
                    }


                    Object textObject =
                            content.get("text");


                    if (textObject != null) {

                        return textObject.toString();
                    }
                }
            }


            // ==================================
            // NO ANSWER FOUND
            // ==================================

            return "Gemini returned no answer.";


        } catch (Exception e) {

            e.printStackTrace();

            return "Gemini Error: "
                    + e.getMessage();
        }
    }
}