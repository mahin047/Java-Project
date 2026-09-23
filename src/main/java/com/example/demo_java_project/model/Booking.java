package com.example.demo_java_project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Booking {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final int id;
    private final int userId;
    private final int resourceId;
    private final String resourceName;
    private final LocalDate bookingDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BookingStatus status;
    private final LocalDateTime createdAt;

    public Booking(int id, int userId, int resourceId, String resourceName,
                   LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
                   BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId()                  { return id; }
    public int getUserId()              { return userId; }
    public int getResourceId()          { return resourceId; }
    public String getResourceName()     { return resourceName; }
    public LocalDate getBookingDate()   { return bookingDate; }
    public LocalTime getStartTime()     { return startTime; }
    public LocalTime getEndTime()       { return endTime; }
    public BookingStatus getStatus()    { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getDateText() { return bookingDate.format(DATE_FMT); }
    public String getTimeText() { return startTime.format(TIME_FMT) + " - " + endTime.format(TIME_FMT); }

    public boolean isCancellable() {
        if (status == BookingStatus.CANCELLED) return false;
        return LocalDateTime.of(bookingDate, startTime).isAfter(LocalDateTime.now());
    }
}