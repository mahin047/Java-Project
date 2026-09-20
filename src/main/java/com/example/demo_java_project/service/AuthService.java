package com.example.demo_java_project.service;

import com.example.demo_java_project.dao.UserDAO;
import com.example.demo_java_project.exception.AuthenticationException;
import com.example.demo_java_project.exception.RegistrationException;
import com.example.demo_java_project.model.Role;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.security.PasswordUtil;
import com.example.demo_java_project.util.Validator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final UserDAO userDAO = new UserDAO();

    // ---------------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------------
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

    // ---------------------------------------------------------------
    // REGISTER (always creates a STUDENT)
    // ---------------------------------------------------------------
    public User register(String fullName, String email, String studentId, String password)
            throws RegistrationException {

        fullName  = fullName  == null ? "" : fullName.trim();
        email     = email     == null ? "" : email.trim().toLowerCase();
        studentId = studentId == null ? "" : studentId.trim();

        // Validate again here: never trust only the UI
        if (!Validator.isValidName(fullName))
            throw new RegistrationException("Please enter a valid full name.");
        if (!Validator.isValidEmail(email))
            throw new RegistrationException("Please enter a valid email address.");
        if (!Validator.isValidStudentId(studentId))
            throw new RegistrationException("Student ID must be 4-30 letters, digits or hyphens.");

        String passwordError = Validator.passwordError(password);
        if (passwordError != null)
            throw new RegistrationException(passwordError);

        try {
            if (userDAO.existsByEmail(email))
                throw new RegistrationException("This email is already registered.");
            if (userDAO.existsByStudentId(studentId))
                throw new RegistrationException("This student ID is already registered.");

            String hash = PasswordUtil.hash(password);
            int id = userDAO.insert(new User(fullName, email, studentId, hash, Role.STUDENT));

            return new User(id, fullName, email, studentId, hash, Role.STUDENT, LocalDateTime.now());

        } catch (SQLException e) {
            // Two people registering the same email at the same instant:
            // the UNIQUE constraint in MySQL is the final safety net.
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new RegistrationException("Email or student ID is already registered.");
            }
            throw new RegistrationException(
                    "Cannot connect to the database. Please try again later.", e);
        }
    }

    // ---------------------------------------------------------------
    // DEV ONLY: test accounts (remove before final submission)
    // ---------------------------------------------------------------
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