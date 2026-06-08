package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users/api")
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllUsers(HttpSession session) {
        System.out.println("=== GET ALL USERS API ===");

        // Check if admin is logged in
        if (session.getAttribute("userId") == null) {
            System.out.println("Unauthorized - no session");
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        List<User> users = userRepository.findAll();
        System.out.println("Found " + users.size() + " users");

        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName() != null ? user.getName() : "");
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
            userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
            userMap.put("role", user.getRole() != null ? user.getRole().toString() : "USER");
            userMap.put("isActive", user.getIsActive() != null ? user.getIsActive() : true);
            userMap.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        return ResponseEntity.ok(response);
    }

    // Create new user
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request, HttpSession session) {
        System.out.println("=== CREATE USER API ===");
        System.out.println("Request: " + request);

        // Check if admin is logged in
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String phone = request.get("phone");
            String role = request.get("role");

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPhone(phone != null ? phone : "");
            user.setRole("ADMIN".equals(role) ? User.Role.ADMIN : User.Role.USER);
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);

            System.out.println("User created successfully: " + email);
            return ResponseEntity.ok(Map.of("success", true, "message", "User created successfully"));
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update user
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpSession session) {
        System.out.println("=== UPDATE USER API ===");
        System.out.println("User ID: " + id);

        // Check if admin is logged in
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.containsKey("name")) {
                user.setName((String) request.get("name"));
            }
            if (request.containsKey("phone")) {
                user.setPhone((String) request.get("phone"));
            }
            if (request.containsKey("role")) {
                String role = (String) request.get("role");
                user.setRole("ADMIN".equals(role) ? User.Role.ADMIN : User.Role.USER);
            }
            if (request.containsKey("isActive")) {
                user.setIsActive((Boolean) request.get("isActive"));
            }
            if (request.containsKey("password") && request.get("password") != null && !((String) request.get("password")).isEmpty()) {
                user.setPassword(passwordEncoder.encode((String) request.get("password")));
            }

            userRepository.save(user);
            System.out.println("User updated successfully: " + user.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "message", "User updated successfully"));
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Activate user
    @PatchMapping("/activate/{id}")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long id, HttpSession session) {
        System.out.println("=== ACTIVATE USER API ===");
        System.out.println("User ID: " + id);

        // Check if admin is logged in
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setIsActive(true);
        userRepository.save(user);
        System.out.println("User activated: " + user.getEmail());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Deactivate user
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long id, HttpSession session) {
        System.out.println("=== DEACTIVATE USER API ===");
        System.out.println("User ID: " + id);

        // Check if admin is logged in
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setIsActive(false);
        userRepository.save(user);
        System.out.println("User deactivated: " + user.getEmail());
        return ResponseEntity.ok(Map.of("success", true));
    }
}