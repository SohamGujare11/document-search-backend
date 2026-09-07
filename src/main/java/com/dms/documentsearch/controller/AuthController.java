package com.dms.documentsearch.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dms.documentsearch.model.User;
import com.dms.documentsearch.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user) {

        try {

            User registeredUser =
                    authService.register(user);

            // Never return password
            registeredUser.setPassword(null);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registeredUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody
            Map<String, String> loginRequest) {

        try {

            String username =
                    loginRequest.get("username");

            String password =
                    loginRequest.get("password");

            // Check username and password
            // and generate JWT
            String token =
                    authService.login(
                            username,
                            password
                    );

            // Get logged-in user information
            User user =
                    authService
                            .findByUsername(username);

            // Build response
            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "token",
                    token
            );

            response.put(
                    "username",
                    user.getUsername()
            );

            response.put(
                    "email",
                    user.getEmail()
            );

            response.put(
                    "role",
                    user.getRole()
            );

            return ResponseEntity
                    .ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(e.getMessage());
        }
    }
}