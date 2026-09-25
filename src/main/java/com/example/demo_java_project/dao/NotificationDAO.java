package com.example.demo_java_project.dao;

import com.example.demo_java_project.database.DatabaseConnection;
import com.example.demo_java_project.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public int insert(int userId, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<Notification> findByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT id, user_id, message, is_read, created_at FROM notifications "
                + "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

        List<Notification> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Notification(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return result;
    }

    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}