package com.example.demo_java_project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDto {

    private String name;
    private String type;
    private String location;
    private int capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String description;
    private String amenities;
    private boolean active;

    public ResourceDto() { }   // Jackson needs a no-arg constructor

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public String getType()             { return type; }
    public void setType(String type)    { this.type = type; }

    public String getLocation()         { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity()            { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public LocalTime getOpenTime()      { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime()     { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public String getDescription()      { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAmenities()        { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public boolean isActive()           { return active; }
    public void setActive(boolean active) { this.active = active; }
}