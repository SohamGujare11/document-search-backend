package com.dms.documentsearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_storage")
public class StorageConfig {

    // ==========================================
    // PRIMARY KEY
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // USERNAME
    // ==========================================

    @Column(nullable = false, unique = true)
    private String username;

    // ==========================================
    // STORAGE PATH
    // ==========================================

    @Column(nullable = false)
    private String storagePath;

    // ==========================================
    // ACTIVE STATUS
    // ==========================================

    @Column(nullable = false)
    private boolean isActive;

    // ==========================================
    // EMPTY CONSTRUCTOR
    // ==========================================

    public StorageConfig() {
    }

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public StorageConfig(
            String username,
            String storagePath,
            boolean isActive) {

        this.username = username;
        this.storagePath = storagePath;
        this.isActive = isActive;
    }

    // ==========================================
    // GET ID
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ==========================================
    // GET USERNAME
    // ==========================================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ==========================================
    // GET STORAGE PATH
    // ==========================================

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    // ==========================================
    // GET ACTIVE
    // ==========================================

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}