package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Display User Management Page
    @GetMapping
    public String userManagementPage(HttpSession session, Model model) {
        // Check if user is logged in and is admin
        if (session.getAttribute("userRole") == null || 
            !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }
        
        model.addAttribute("adminName", session.getAttribute("userName"));
        model.addAttribute("pageTitle", "User Management");
        return "admin/users";
    }

    // API: Get all users
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllUsers(HttpSession session) {
        // Check admin permission
        if (session.getAttribute("userRole") == null || 
            !"ADMIN".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", users.size());
        return ResponseEntity.ok(response);
    }

    // API: Get single user by ID
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    // API: Create new user
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            String email = request.get("email");
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }
            
            User user = new User();
            user.setName(request.get("name"));
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.get("password")));
            user.setPhone(request.get("phone"));
            user.setRole(User.Role.valueOf(request.get("role")));
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User created successfully",
                "user", savedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    // API: Update user
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setName((String) request.get("name"));
            user.setPhone((String) request.get("phone"));
            user.setRole(User.Role.valueOf((String) request.get("role")));
            user.setIsActive((Boolean) request.get("isActive"));
            
            String password = (String) request.get("password");
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "user", updatedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update user: " + e.getMessage()));
        }
    }

    // API: Soft delete user (set isActive to false)
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to deactivate user: " + e.getMessage()));
        }
    }
    
    // API: Activate user
    @PatchMapping("/api/activate/{id}")
    @ResponseBody
    public ResponseEntity<?> activateUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setIsActive(true);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User activated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to activate user"));
        }
    }
    
    // API: Deactivate user
    @PatchMapping("/api/deactivate/{id}")
    @ResponseBody
    public ResponseEntity<?> deactivateUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to deactivate user"));
        }
    }
    
    // Helper method to check admin
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("userRole") != null &&
                "ADMIN".equals(session.getAttribute("userRole"));
    }
}