package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EmergencyContact implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("icon")
    private String icon;

    public EmergencyContact() {}

    public EmergencyContact(String name, String phoneNumber, String description, String category) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.category = category;
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