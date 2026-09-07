package com.dms.documentsearch.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dms.documentsearch.entity.UserStorage;
import com.dms.documentsearch.model.User;
import com.dms.documentsearch.repository.UserRepository;
import com.dms.documentsearch.repository.UserStorageRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserStorageRepository userStorageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       UserStorageRepository userStorageRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.userStorageRepository = userStorageRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================
    // REGISTER USER
    // =========================
    public User register(User user) {

        // Check whether username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check whether email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encrypt password before storing it in MySQL
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Every public registration is always USER
        user.setRole("USER");

        // Save user in database
        User savedUser = userRepository.save(user);

        // Create user folder path
        String userFolder =
                "C:/Users/SOHAM/OneDrive/Desktop/DocumentStorage/"
                        + savedUser.getUsername();

        // Create folder if it doesn't exist
        try {
            Path folder = Paths.get(userFolder);

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to create user storage folder", e);
        }

        // Save storage configuration
        UserStorage storage = new UserStorage();
        storage.setUsername(savedUser.getUsername());
        storage.setStoragePath(userFolder);
        storage.setIsActive(true);

        userStorageRepository.save(storage);

        return savedUser;
    }

    // =========================
    // LOGIN USER
    // =========================
    public String login(String username, String password) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return jwtService.generateToken(user);
    }

    // =========================
    // FIND USER
    // =========================
    public User findByUsername(String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}