package com.dms.documentsearch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dms.documentsearch.entity.UserStorage;

public interface UserStorageRepository extends JpaRepository<UserStorage, Long> {

    Optional<UserStorage> findByUsername(String username);

}