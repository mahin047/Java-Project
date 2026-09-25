package com.example.demo_java_project.service;

import com.example.demo_java_project.dao.NotificationDAO;
import com.example.demo_java_project.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    /** Best-effort: a notification failure should never break the action that triggered it. */
    public void notify(int userId, String message) {
        try {
            notificationDAO.insert(userId, message);
        } catch (SQLException e) {
            System.err.println("Could not save notification: " + e.getMessage());
        }
    }

    public List<Notification> getMyNotifications(int userId, int limit) throws SQLException {
        return notificationDAO.findByUser(userId, limit);
    }

    public int countUnread(int userId) throws SQLException {
        return notificationDAO.countUnread(userId);
    }

    public void markAllRead(int userId) throws SQLException {
        notificationDAO.markAllRead(userId);
    }
}