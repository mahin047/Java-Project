package com.example.demo_java_project.model;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String studentId;          // may be null (e.g. admin)
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;

    /** For users read from the database. */
    public User(int id, String fullName, String email, String studentId,
                String passwordHash, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    /** For new users (id and createdAt are set by the database). */
    public User(String fullName, String email, String studentId,
                String passwordHash, Role role) {
        this(0, fullName, email, studentId, passwordHash, role, null);
    }

    public int getId()                  { return id; }
    public String getFullName()         { return fullName; }
    public String getEmail()            { return email; }
    public String getStudentId()        { return studentId; }
    public String getPasswordHash()     { return passwordHash; }
    public Role getRole()               { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + "}";
    }
}