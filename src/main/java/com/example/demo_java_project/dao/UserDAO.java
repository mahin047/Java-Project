package com.example.demo_java_project.dao;

import com.example.demo_java_project.database.DatabaseConnection;
import com.example.demo_java_project.model.Role;
import com.example.demo_java_project.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDAO {

    private static final String COLUMNS =
            "id, full_name, email, student_id, password_hash, role, created_at";

    /** Login accepts either email or student ID. */
    public Optional<User> findByEmailOrStudentId(String identifier) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users "
                + "WHERE email = ? OR student_id = ? LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public boolean existsByStudentId(String studentId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE student_id = ? LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a user and returns the generated id. */
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, student_id, password_hash, role) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getStudentId());   // null is stored as NULL
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("student_id"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}