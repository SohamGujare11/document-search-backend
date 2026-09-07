package com.dms.documentsearch.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.dms.documentsearch.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ==========================================
    // SIGNING KEY
    // ==========================================

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ==========================================
    // GENERATE TOKEN
    // ==========================================

    public String generateToken(User user) {

        return Jwts.builder()

                .subject(user.getUsername())

                .claim(
                        "role",
                        user.getRole()
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtExpiration
                        )
                )

                .signWith(
                        getSigningKey()
                )

                .compact();
    }

    // ==========================================
    // EXTRACT ALL CLAIMS
    // ==========================================

    private Claims extractAllClaims(
            String token) {

        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }

    // ==========================================
    // EXTRACT USERNAME
    // ==========================================

    public String extractUsername(
            String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // ==========================================
    // EXTRACT ROLE
    // ==========================================

    public String extractRole(
            String token) {

        return extractAllClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    // ==========================================
    // CHECK EXPIRATION
    // ==========================================

    public boolean isTokenExpired(
            String token) {

        Date expiration =
                extractAllClaims(token)
                        .getExpiration();

        return expiration.before(
                new Date()
        );
    }

    // ==========================================
    // VALIDATE TOKEN
    // ==========================================

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        try {

            String username =
                    extractUsername(token);

            return username != null
                    && username.equals(
                            userDetails.getUsername()
                    )
                    && !isTokenExpired(token);

        } catch (Exception e) {

            System.out.println(
                    "JWT validation error: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // ==========================================
    // BACKWARD COMPATIBILITY
    // ==========================================

    public boolean isTokenValid(
            String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }
}