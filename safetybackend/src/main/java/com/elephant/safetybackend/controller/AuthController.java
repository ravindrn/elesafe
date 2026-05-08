package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.AuthRequest;
import com.elephant.safetybackend.dto.AuthResponse;
import com.elephant.safetybackend.dto.RegisterRequest;
import com.elephant.safetybackend.model.User;
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
}