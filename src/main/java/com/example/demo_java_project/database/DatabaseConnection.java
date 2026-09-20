package com.example.demo_java_project.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates MySQL connections using values from db.properties.
 * Each call returns a NEW connection, so it is safe to use from
 * multiple threads (each thread must close its own connection).
 *
 * Usage:
 *   try (Connection con = DatabaseConnection.getConnection()) { ... }
 */
public final class DatabaseConnection {

    private static final String CONFIG_PATH =
            "/com/example/demo_java_project/db.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(CONFIG_PATH)) {
            if (in == null) {
                throw new IllegalStateException(
                        "db.properties not found. Copy db.properties.example to db.properties "
                                + "and fill in your MySQL details.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }

        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");

        if (URL == null || USER == null || PASSWORD == null) {
            throw new IllegalStateException(
                    "db.properties must contain db.url, db.user and db.password");
        }
    }

    private DatabaseConnection() { }   // utility class

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick health check, returns true if MySQL is reachable. */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con.isValid(3);
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            return false;
        }
    }
}