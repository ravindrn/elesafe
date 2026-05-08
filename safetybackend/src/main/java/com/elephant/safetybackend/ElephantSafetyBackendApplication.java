package com.elephant.safetybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElephantSafetyBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElephantSafetyBackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("🐘 Elephant Safety Backend Started!");
        System.out.println("📍 Server running at: http://localhost:8080");
        System.out.println("========================================");
    }
}