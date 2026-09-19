package com.example.demo_java_project.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {

            System.out.println("✅ Connected to: " + con.getCatalog());

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM resources")) {
                if (rs.next()) {
                    System.out.println("✅ Resources in table: " + rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed: " + e.getMessage());
        }
    }
}