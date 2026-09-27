package com.example.demo_java_project.service;

import com.example.demo_java_project.exception.PermissionDeniedException;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;

/**
 * Base class for services that restrict some of their actions to
 * administrators. ResourceService and AdminService both extend this
 * instead of each repeating the same admin check.
 */
public abstract class AdminRestrictedService {

    protected boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && user.isAdmin();
    }

    protected void requireAdmin() throws PermissionDeniedException {
        if (!isAdmin()) {
            throw new PermissionDeniedException("Only administrators can perform this action.");
        }
    }
}