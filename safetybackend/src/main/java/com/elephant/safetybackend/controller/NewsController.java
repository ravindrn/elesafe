package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.NewsItem;
import com.elephant.safetybackend.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/news")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @GetMapping
    public String newsPage(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/news";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<NewsItem>> getAllNews(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(newsRepository.findAll());
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addNews(@RequestBody NewsItem news, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        try {
            news.setId(null); // Ensure new record
            news.setCreatedAt(LocalDateTime.now());
            news.setPublishedDate(LocalDateTime.now());
            news.setIsActive(true);
            NewsItem saved = newsRepository.save(news);
            return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateNews(@PathVariable Long id, @RequestBody NewsItem news, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        try {
            NewsItem existing = newsRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            existing.setTitle(news.getTitle());
            existing.setContent(news.getContent());
            existing.setSource(news.getSource());
            existing.setImageUrl(news.getImageUrl());
            existing.setType(news.getType());
            existing.setIsActive(news.getIsActive());
            existing.setUpdatedAt(LocalDateTime.now());

            newsRepository.save(existing);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteNews(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        newsRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}