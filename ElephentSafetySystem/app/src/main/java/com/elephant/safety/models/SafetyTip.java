package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;

public class SafetyTip {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("icon")
    private String icon;

    @SerializedName("priority")
    private Integer priority;

    // Default constructor
    public SafetyTip() {}

    // Constructor for creating new tips
    public SafetyTip(String title, String description, String category, String icon) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
    }

    // Full constructor
    public SafetyTip(Long id, String title, String description, String category, String icon, Integer priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.priority = priority;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getPriority() {
        return priority;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}