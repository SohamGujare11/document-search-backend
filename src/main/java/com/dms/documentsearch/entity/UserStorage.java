package com.dms.documentsearch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_storage")
public class UserStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private Boolean isActive = true;

    public UserStorage() {
    }

    public UserStorage(String username, String storagePath, Boolean isActive) {
        this.username = username;
        this.storagePath = storagePath;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}