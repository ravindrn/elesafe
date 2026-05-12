package com.elephant.safetybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple view mappings without controller needed
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/admin/dashboard").setViewName("dashboard");
    }
}