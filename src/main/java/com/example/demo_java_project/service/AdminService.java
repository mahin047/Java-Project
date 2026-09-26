package com.example.demo_java_project.service;

import com.example.demo_java_project.dao.BookingDAO;
import com.example.demo_java_project.dao.ResourceDAO;
import com.example.demo_java_project.dao.UserDAO;
import com.example.demo_java_project.exception.PermissionDeniedException;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Role;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class AdminService {

    private final UserDAO userDAO = new UserDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public Overview getOverview() throws ServiceException {
        requireAdmin();
        try {
            bookingDAO.completePastBookings();
            int totalStudents  = userDAO.countByRole(Role.STUDENT);
            int totalAdmins    = userDAO.countByRole(Role.ADMIN);
            int totalResources = resourceDAO.countActive();
            int totalBookings  = bookingDAO.countAllActive();
            List<BookingDAO.BookingSummary> recent = bookingDAO.findRecentForAdmin(10);

            return new Overview(totalStudents, totalAdmins, totalResources, totalBookings, recent);

        } catch (SQLException e) {
            throw new ServiceException("Could not load admin overview. Please try again.", e);
        }
    }

    private void requireAdmin() throws PermissionDeniedException {
        User user = SessionManager.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            throw new PermissionDeniedException("Only administrators can view this page.");
        }
    }

    public static class Overview {
        private final int totalStudents;
        private final int totalAdmins;
        private final int totalResources;
        private final int totalBookings;
        private final List<BookingDAO.BookingSummary> recentBookings;

        public Overview(int totalStudents, int totalAdmins, int totalResources,
                        int totalBookings, List<BookingDAO.BookingSummary> recentBookings) {
            this.totalStudents = totalStudents;
            this.totalAdmins = totalAdmins;
            this.totalResources = totalResources;
            this.totalBookings = totalBookings;
            this.recentBookings = recentBookings;
        }

        public int getTotalStudents()  { return totalStudents; }
        public int getTotalAdmins()    { return totalAdmins; }
        public int getTotalResources() { return totalResources; }
        public int getTotalBookings()  { return totalBookings; }
        public List<BookingDAO.BookingSummary> getRecentBookings() { return recentBookings; }
    }
}