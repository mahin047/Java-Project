package com.example.demo_java_project.service;

import com.example.demo_java_project.dao.ResourceDAO;
import com.example.demo_java_project.exception.PermissionDeniedException;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class ResourceService {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final ResourceDAO resourceDAO = new ResourceDAO();

    // ---------------------------------------------------------------
    // READ (everyone). Admins also see inactive resources.
    // ---------------------------------------------------------------
    public List<Resource> getResources(String keyword, String type) throws ServiceException {
        try {
            return resourceDAO.search(keyword, type, isAdmin());
        } catch (SQLException e) {
            throw new ServiceException("Could not load resources. Please try again.", e);
        }
    }

    // ---------------------------------------------------------------
    // WRITE (admin only)
    // ---------------------------------------------------------------
    public void addResource(Resource draft) throws ServiceException {
        requireAdmin();
        Resource r = validated(draft);

        try {
            if (resourceDAO.existsByName(r.getName(), 0)) {
                throw new ServiceException("A resource named \"" + r.getName() + "\" already exists.");
            }
            resourceDAO.insert(r);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    public void updateResource(Resource changed) throws ServiceException {
        requireAdmin();
        Resource r = validated(changed);

        try {
            if (resourceDAO.existsByName(r.getName(), r.getId())) {
                throw new ServiceException("Another resource is already named \"" + r.getName() + "\".");
            }
            resourceDAO.update(r);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    public void setActive(int resourceId, boolean active) throws ServiceException {
        requireAdmin();
        try {
            resourceDAO.setActive(resourceId, active);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && user.isAdmin();
    }

    private void requireAdmin() throws PermissionDeniedException {
        if (!isAdmin()) {
            throw new PermissionDeniedException("Only administrators can perform this action.");
        }
    }

    /** Trims, checks every rule, and returns a clean copy. */
    private Resource validated(Resource r) throws ServiceException {
        String name        = r.getName() == null ? "" : r.getName().trim();
        String type        = r.getType() == null ? "" : r.getType().trim();
        String location    = r.getLocation().trim();
        String description = r.getDescription().trim();

        if (name.length() < 2 || name.length() > 100)
            throw new ServiceException("Name must be 2-100 characters.");
        if (!Resource.TYPES.contains(type))
            throw new ServiceException("Please choose a valid resource type.");
        if (r.getCapacity() < 1 || r.getCapacity() > 1000)
            throw new ServiceException("Capacity must be between 1 and 1000.");
        if (location.length() > 100)
            throw new ServiceException("Location must be at most 100 characters.");
        if (description.length() > 255)
            throw new ServiceException("Description must be at most 255 characters.");

        return new Resource(r.getId(), name, type, location,
                r.getCapacity(), description, r.isActive());
    }

    private ServiceException translate(SQLException e) {
        if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
            return new ServiceException("A resource with this name already exists.");
        }
        return new ServiceException("Database error. Please try again later.", e);
    }
}