package com.dms.documentsearch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ORIGINAL DOCUMENT
    // ==========================================

    @Column(nullable = false)
    private Long documentId;


    // ==========================================
    // DOCUMENT OWNER
    // ==========================================

    @Column(nullable = false)
    private String ownerUsername;


    // ==========================================
    // CHUNK POSITION
    // ==========================================

    @Column(nullable = false)
    private Integer chunkIndex;


    // ==========================================
    // CHUNK TEXT
    // ==========================================

    @Lob
    @Column(
            columnDefinition = "LONGTEXT",
            nullable = false
    )
    private String chunkText;


    // ==========================================
    // EMBEDDING VECTOR
    // ==========================================

    /*
     * Gemini returns an array of numbers.
     *
     * For our first RAG implementation we store
     * that vector as JSON:
     *
     * [0.123,-0.456,0.789,...]
     *
     * Later we deserialize it and calculate
     * cosine similarity.
     */

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String embedding;


    // ==========================================
    // EMPTY CONSTRUCTOR
    // ==========================================

    public DocumentChunk() {
    }


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DocumentChunk(
            Long documentId,
            String ownerUsername,
            Integer chunkIndex,
            String chunkText) {

        this.documentId = documentId;
        this.ownerUsername = ownerUsername;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
    }


    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getDocumentId() {
        return documentId;
    }


    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }


    public String getOwnerUsername() {
        return ownerUsername;
    }


    public void setOwnerUsername(
            String ownerUsername) {

        this.ownerUsername = ownerUsername;
    }


    public Integer getChunkIndex() {
        return chunkIndex;
    }


    public void setChunkIndex(
            Integer chunkIndex) {

        this.chunkIndex = chunkIndex;
    }


    public String getChunkText() {
        return chunkText;
    }


    public void setChunkText(
            String chunkText) {

        this.chunkText = chunkText;
    }


    public String getEmbedding() {
        return embedding;
    }


    public void setEmbedding(
            String embedding) {

        this.embedding = embedding;
    }
}