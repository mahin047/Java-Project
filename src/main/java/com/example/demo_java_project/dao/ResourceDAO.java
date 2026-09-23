package com.example.demo_java_project.dao;
import java.util.Optional;
import com.example.demo_java_project.database.DatabaseConnection;
import com.example.demo_java_project.model.Resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    private static final String COLUMNS =
            "id, name, type, location, capacity, open_time, close_time, " +
                    "description, amenities, is_active";

    /** Search by name/location and (optionally) type. Inactive ones only if asked. */
    public List<Resource> search(String keyword, String type, boolean includeInactive)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM resources WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (!includeInactive) {
            sql.append(" AND is_active = TRUE");
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR location LIKE ?)");
            String like = "%" + escapeLike(keyword.trim()) + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY is_active DESC, name");

        List<Resource> result = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }
    public Optional<Resource> findById(int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM resources WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** excludeId lets an edit keep its own name (use 0 when adding). */
    public boolean existsByName(String name, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM resources WHERE name = ? AND id <> ? LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(Resource r) throws SQLException {
        String sql = "INSERT INTO resources (name, type, location, capacity, description, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getType());
            ps.setString(3, r.getLocation());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getDescription());
            ps.setBoolean(6, r.isActive());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(Resource r) throws SQLException {
        String sql = "UPDATE resources SET name = ?, type = ?, location = ?, capacity = ?, "
                + "description = ?, is_active = ? WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getType());
            ps.setString(3, r.getLocation());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getDescription());
            ps.setBoolean(6, r.isActive());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
        }
    }

    public void setActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE resources SET is_active = ? WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Used by the dashboard. */
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM resources WHERE is_active = TRUE";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---------------------------------------------------------------
    private Resource mapRow(ResultSet rs) throws SQLException {

        java.sql.Time open = rs.getTime("open_time");
        java.sql.Time close = rs.getTime("close_time");

        return new Resource(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("location"),
                rs.getInt("capacity"),
                open != null ? open.toLocalTime() : null,
                close != null ? close.toLocalTime() : null,
                rs.getString("description"),
                rs.getString("amenities"),
                rs.getBoolean("is_active")
        );
    }

    /** So that a user typing % or _ searches for the literal character. */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}