package com.elephant.safetybackend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NewsItemDTO {
    private Long id;
    private String title;
    private String content;
    private String source;
    private String imageUrl;
    private String type;
    private String date;

    public NewsItemDTO() {}

    public NewsItemDTO(Long id, String title, String content, String source,
                       String imageUrl, String type, LocalDateTime publishedDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.source = source;
        this.imageUrl = imageUrl;
        this.type = type;
        this.date = formatDate(publishedDate);
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}