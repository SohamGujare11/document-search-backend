package com.dms.documentsearch.controller;

import com.dms.documentsearch.entity.Document;

public class UploadResponse {

    private Document document;

    private double processingTimeSeconds;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public UploadResponse(
            Document document,
            double processingTimeSeconds) {

        this.document =
                document;

        this.processingTimeSeconds =
                processingTimeSeconds;
    }


    // ==========================================
    // GET DOCUMENT
    // ==========================================

    public Document getDocument() {

        return document;
    }


    // ==========================================
    // GET PROCESSING TIME
    // ==========================================

    public double getProcessingTimeSeconds() {

        return processingTimeSeconds;
    }
}