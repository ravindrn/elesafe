package com.elephant.safetybackend.controller;

import java.util.Map;
import java.time.LocalDateTime;

import com.elephant.safetybackend.dto.AuthRequest;
import com.elephant.safetybackend.dto.AuthResponse;
import com.elephant.safetybackend.dto.RegisterRequest;
import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.DangerZoneRepository;
import com.elephant.safetybackend.repository.UserRepository;
import com.elephant.safetybackend.service.UserService;
import com.elephant.safetybackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setPhone(request.getPhone());

            User savedUser = userService.register(user);
            String token = jwtUtil.generateToken(savedUser.getEmail());

            AuthResponse.UserDTO userDTO = new AuthResponse.UserDTO(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getRole().toString()
            );

            return ResponseEntity.ok(new AuthResponse(token, userDTO));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        User user = userService.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        AuthResponse.UserDTO userDTO = new AuthResponse.UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );

        return ResponseEntity.ok(new AuthResponse(token, userDTO));
    }

    // ========== LOCATION UPDATE ENDPOINT ==========
    @PostMapping("/update-location")
    public ResponseEntity<?> updateUserLocation(@RequestBody Map<String, Double> locationData,
                                                @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== LOCATION UPDATE RECEIVED ===");
            System.out.println("Location data: " + locationData);
            
            // Extract email from token (remove "Bearer " prefix if present)
            String jwtToken = token;
            if (token.startsWith("Bearer ")) {
                jwtToken = token.substring(7);
            }
            
            String email = jwtUtil.extractUsername(jwtToken);
            System.out.println("User email: " + email);
            
            User user = userService.findByEmail(email).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }
            
            Double latitude = locationData.get("latitude");
            Double longitude = locationData.get("longitude");
            
            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);
            
            // Update user location
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            user.setLastLocationUpdate(LocalDateTime.now());
            userRepository.save(user);
            
            System.out.println("Location updated for user: " + user.getName());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Location updated successfully"
            ));
            
        } catch (Exception e) {
            System.out.println("Error updating location: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update location: " + e.getMessage()));
        }
    }
}