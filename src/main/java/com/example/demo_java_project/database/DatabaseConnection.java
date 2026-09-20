package com.example.demo_java_project.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates MySQL connections using values from db.properties.
 * Each call returns a NEW connection, so it is safe to use from
 * multiple threads (each thread must close its own connection).
 *
 * Config lookup order:
 *   1. classpath: /com/example/demo_java_project/db.properties
 *   2. classpath: /db.properties
 *   3. file:      ./db.properties  (project root folder)
 */
public final class DatabaseConnection {

    private static final String[] CLASSPATH_LOCATIONS = {
            "/com/example/demo_java_project/db.properties",
            "/db.properties"
    };
    private static final Path EXTERNAL_FILE = Path.of("db.properties");

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = loadProperties();

        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");

        if (URL == null || USER == null || PASSWORD == null) {
            throw new IllegalStateException(
                    "db.properties must contain db.url, db.user and db.password");
        }
    }

    private DatabaseConnection() { }

    private static Properties loadProperties() {
        Properties props = new Properties();

        try {
            for (String location : CLASSPATH_LOCATIONS) {
                try (InputStream in = DatabaseConnection.class.getResourceAsStream(location)) {
                    if (in != null) {
                        props.load(in);
                        return props;
                    }
                }
            }

            if (Files.exists(EXTERNAL_FILE)) {
                try (InputStream in = Files.newInputStream(EXTERNAL_FILE)) {
                    props.load(in);
                    return props;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }

        throw new IllegalStateException(
                "db.properties not found. Looked in classpath "
                        + String.join(", ", CLASSPATH_LOCATIONS)
                        + " and in " + EXTERNAL_FILE.toAbsolutePath()
                        + ". Copy db.properties.example to db.properties and fill in your MySQL details.");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con.isValid(3);
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            return false;
        }
    }
}