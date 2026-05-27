package com.elephant.safetybackend.dto;

public class EmergencyContactDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String description;
    private String category;
    private Integer priority;
    private String icon;

    // Constructors
    public EmergencyContactDTO() {}

    public EmergencyContactDTO(Long id, String name, String phoneNumber, String description, String category, Integer priority, String icon) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.icon = icon;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}