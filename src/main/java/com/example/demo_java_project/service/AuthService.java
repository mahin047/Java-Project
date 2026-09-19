package com.example.demo_java_project.service;

import com.example.demo_java_project.dao.UserDAO;
import com.example.demo_java_project.exception.AuthenticationException;
import com.example.demo_java_project.model.Role;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.security.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Verifies credentials. The same generic message is used for
     * "user not found" and "wrong password" so attackers can't
     * discover which emails exist.
     */
    public User login(String identifier, String password) throws AuthenticationException {
        try {
            Optional<User> found = userDAO.findByEmailOrStudentId(identifier.trim());

            if (found.isEmpty()
                    || !PasswordUtil.verify(password, found.get().getPasswordHash())) {
                throw new AuthenticationException("Invalid email / student ID or password.");
            }
            return found.get();

        } catch (SQLException e) {
            throw new AuthenticationException(
                    "Cannot connect to the database. Please try again later.", e);
        }
    }

    /**
     * DEVELOPMENT ONLY: creates two test accounts if they don't exist yet.
     * Remove (or change the passwords) before the final submission.
     */
    public void seedDefaultUsers() throws SQLException {
        createIfMissing("System Admin", "admin@slotsync.com", null,      "admin123",   Role.ADMIN);
        createIfMissing("Demo Student", "student@slotsync.com", "2101001", "student123", Role.STUDENT);
    }

    private void createIfMissing(String name, String email, String studentId,
                                 String plainPassword, Role role) throws SQLException {
        if (!userDAO.existsByEmail(email)) {
            userDAO.insert(new User(name, email, studentId,
                    PasswordUtil.hash(plainPassword), role));
        }
    }
}