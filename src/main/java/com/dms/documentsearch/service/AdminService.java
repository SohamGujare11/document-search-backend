package com.dms.documentsearch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dms.documentsearch.model.User;
import com.dms.documentsearch.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(
            UserRepository userRepository) {

        this.userRepository =
                userRepository;
    }


    // ==========================================
    // GET ALL REGISTERED USERS
    // ==========================================

    public List<User> getAllUsers() {

        List<User> users =
                userRepository.findAll();

        // Never send password hashes to frontend
        users.forEach(
                user ->
                    user.setPassword(null)
        );

        return users;
    }


    // ==========================================
    // PROMOTE USER TO ADMIN
    // ==========================================

    public User makeAdmin(Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                            () ->
                                new RuntimeException(
                                    "User not found"
                                )
                        );

        // Change role
        user.setRole("ADMIN");

        // Save updated user
        User updatedUser =
                userRepository.save(user);

        // Never return password
        updatedUser.setPassword(null);

        return updatedUser;
    }


    // ==========================================
    // CHANGE ADMIN BACK TO USER
    // ==========================================

    public User makeUser(Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                            () ->
                                new RuntimeException(
                                    "User not found"
                                )
                        );

        // Change role back to USER
        user.setRole("USER");

        // Save updated user
        User updatedUser =
                userRepository.save(user);

        // Never return password
        updatedUser.setPassword(null);

        return updatedUser;
    }
}