package com.example.demo_java_project.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Resource {

    public static final List<String> TYPES = List.of("LAB", "ROOM", "EQUIPMENT", "OTHER");

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final int id;
    private final String name;
    private final String type;
    private final String location;
    private final int capacity;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final String description;
    private final String amenities;
    private final boolean active;

    public Resource(int id, String name, String type, String location, int capacity,
                    LocalTime openTime, LocalTime closeTime,
                    String description, String amenities, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location == null ? "" : location;
        this.capacity = capacity;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description == null ? "" : description;
        this.amenities = amenities == null ? "" : amenities;
        this.active = active;
    }

    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getType()          { return type; }
    public String getLocation()      { return location; }
    public int getCapacity()         { return capacity; }
    public LocalTime getOpenTime()   { return openTime; }
    public LocalTime getCloseTime()  { return closeTime; }
    public String getDescription()   { return description; }
    public String getAmenities()     { return amenities; }
    public boolean isActive()        { return active; }

    /** e.g. "8:00 AM - 8:00 PM" */
    public String getHoursText() {
        if (openTime == null || closeTime == null) return "";
        return openTime.format(TIME_FMT) + " - " + closeTime.format(TIME_FMT);
    }

    /** Amenities split into a clean list, e.g. "AC, WiFi" -> ["AC", "WiFi"] */
    public List<String> getAmenityList() {
        if (amenities == null || amenities.isBlank()) return List.of();
        return Arrays.stream(amenities.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public String toString() {
        return name;
    }
}