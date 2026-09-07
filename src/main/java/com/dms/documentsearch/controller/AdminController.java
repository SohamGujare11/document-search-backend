package com.dms.documentsearch.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dms.documentsearch.model.User;
import com.dms.documentsearch.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AdminController {

    private final AdminService adminService;

    public AdminController(
            AdminService adminService) {

        this.adminService = adminService;
    }


    // ==========================================
    // GET ALL USERS
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<List<User>>
    getAllUsers() {

        return ResponseEntity.ok(
                adminService.getAllUsers()
        );
    }


    // ==========================================
    // PROMOTE USER TO ADMIN
    // ==========================================

    @PutMapping("/users/{id}/make-admin")
    public ResponseEntity<User>
    makeAdmin(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminService.makeAdmin(id)
        );
    }


    // ==========================================
    // CHANGE ADMIN BACK TO USER
    // ==========================================

    @PutMapping("/users/{id}/make-user")
    public ResponseEntity<User>
    makeUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminService.makeUser(id)
        );
    }
}