package com.example.demo_java_project.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM, h:mm a");

    private final int id;
    private final int userId;
    private final String message;
    private final boolean read;
    private final LocalDateTime createdAt;

    public Notification(int id, int userId, String message, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId()                  { return id; }
    public int getUserId()              { return userId; }
    public String getMessage()          { return message; }
    public boolean isRead()             { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getTimeText() { return createdAt.format(FMT); }
}