package com.example.demo_java_project.model;

import java.util.List;

public class Resource {

    public static final List<String> TYPES = List.of("LAB", "ROOM", "EQUIPMENT", "OTHER");

    private final int id;
    private final String name;
    private final String type;
    private final String location;
    private final int capacity;
    private final String description;
    private final boolean active;

    public Resource(int id, String name, String type, String location,
                    int capacity, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location == null ? "" : location;
        this.capacity = capacity;
        this.description = description == null ? "" : description;
        this.active = active;
    }

    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getType()        { return type; }
    public String getLocation()    { return location; }
    public int getCapacity()       { return capacity; }
    public String getDescription() { return description; }
    public boolean isActive()      { return active; }

    @Override
    public String toString() {
        return name;
    }
}