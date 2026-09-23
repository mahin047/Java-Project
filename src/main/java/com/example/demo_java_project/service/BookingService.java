package com.example.demo_java_project.service;

import com.example.demo_java_project.concurrency.BookingLockManager;
import com.example.demo_java_project.dao.BookingDAO;
import com.example.demo_java_project.dao.ResourceDAO;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Booking;
import com.example.demo_java_project.model.BookingStatus;
import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingService {

    private static final int SLOT_MINUTES = 60;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();

    /** All hourly slots for a resource on a date, each marked free / booked / past. */
    public List<Slot> getSlots(int resourceId, LocalDate date) throws ServiceException {
        Resource resource = getResourceOrThrow(resourceId);

        try {
            List<LocalTime> booked = bookingDAO.findBookedStartTimes(resourceId, date);
            List<Slot> slots = new ArrayList<>();

            LocalTime cursor = resource.getOpenTime();
            while (!cursor.plusMinutes(SLOT_MINUTES).isAfter(resource.getCloseTime())) {
                boolean isPast = LocalDateTime.of(date, cursor).isBefore(LocalDateTime.now());
                boolean isBooked = booked.contains(cursor);
                slots.add(new Slot(cursor, cursor.plusMinutes(SLOT_MINUTES), isBooked, isPast));
                cursor = cursor.plusMinutes(SLOT_MINUTES);
            }
            return slots;

        } catch (SQLException e) {
            throw new ServiceException("Could not load availability. Please try again.", e);
        }
    }

    /**
     * Books a slot. Thread-safe in two layers:
     *  1) BookingLockManager serializes threads within this app instance.
     *  2) The database's unique constraint catches any race across instances.
     */
    public Booking book(int resourceId, LocalDate date, LocalTime startTime) throws ServiceException {
        User user = requireLogin();
        Resource resource = getResourceOrThrow(resourceId);
        validateRequest(resource, date, startTime);

        try {
            return BookingLockManager.runLocked(resourceId, () -> {
                List<LocalTime> booked = bookingDAO.findBookedStartTimes(resourceId, date);
                if (booked.contains(startTime)) {
                    throw new ServiceException("This slot was just booked by someone else. Please choose another.");
                }

                LocalTime endTime = startTime.plusMinutes(SLOT_MINUTES);
                int id = bookingDAO.insert(user.getId(), resourceId, date, startTime, endTime);

                return new Booking(id, user.getId(), resourceId, resource.getName(),
                        date, startTime, endTime, BookingStatus.CONFIRMED, LocalDateTime.now());
            });

        } catch (ServiceException e) {
            throw e;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("This slot was just booked by someone else. Please choose another.");
        } catch (Exception e) {
            throw new ServiceException("Could not complete the booking. Please try again.", e);
        }
    }

    public List<Booking> getMyBookings() throws ServiceException {
        User user = requireLogin();
        try {
            return bookingDAO.findByUser(user.getId());
        } catch (SQLException e) {
            throw new ServiceException("Could not load your bookings. Please try again.", e);
        }
    }

    public void cancelBooking(Booking booking) throws ServiceException {
        User user = requireLogin();
        if (booking.getUserId() != user.getId() && !user.isAdmin()) {
            throw new ServiceException("You can only cancel your own bookings.");
        }
        if (!booking.isCancellable()) {
            throw new ServiceException("This booking can no longer be cancelled.");
        }
        try {
            bookingDAO.cancel(booking.getId(), booking.getUserId());
        } catch (SQLException e) {
            throw new ServiceException("Could not cancel the booking. Please try again.", e);
        }
    }

    // ---------------------------------------------------------------
    private void validateRequest(Resource resource, LocalDate date, LocalTime startTime) throws ServiceException {
        if (date == null || startTime == null)
            throw new ServiceException("Please select a date and time slot.");
        if (LocalDateTime.of(date, startTime).isBefore(LocalDateTime.now()))
            throw new ServiceException("You cannot book a slot in the past.");
        if (startTime.isBefore(resource.getOpenTime())
                || startTime.plusMinutes(SLOT_MINUTES).isAfter(resource.getCloseTime()))
            throw new ServiceException("Selected time is outside " + resource.getName() + "'s hours.");
        if (!resource.isActive())
            throw new ServiceException("This resource is currently inactive.");
    }

    private Resource getResourceOrThrow(int resourceId) throws ServiceException {
        try {
            Optional<Resource> r = resourceDAO.findById(resourceId);
            if (r.isEmpty()) throw new ServiceException("Resource not found.");
            return r.get();
        } catch (SQLException e) {
            throw new ServiceException("Could not load resource details.", e);
        }
    }

    private User requireLogin() throws ServiceException {
        User user = SessionManager.getCurrentUser();
        if (user == null) throw new ServiceException("Please log in again.");
        return user;
    }

    /** Simple value holder for the booking screen. */
    public static class Slot {
        private final LocalTime start;
        private final LocalTime end;
        private final boolean booked;
        private final boolean past;

        public Slot(LocalTime start, LocalTime end, boolean booked, boolean past) {
            this.start = start;
            this.end = end;
            this.booked = booked;
            this.past = past;
        }

        public LocalTime getStart()  { return start; }
        public LocalTime getEnd()    { return end; }
        public boolean isBooked()    { return booked; }
        public boolean isPast()      { return past; }
    }
}