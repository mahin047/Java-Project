package com.example.demo_java_project.dao;

import com.example.demo_java_project.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResourceDAO {

    /** Number of resources that can currently be booked. */
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM resources WHERE is_active = TRUE";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}