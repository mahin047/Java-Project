package com.example.demo_java_project.dao;

import com.example.demo_java_project.database.DatabaseConnection;
import com.example.demo_java_project.model.Booking;
import com.example.demo_java_project.model.BookingStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private static final String SELECT_WITH_RESOURCE =
            "SELECT b.id, b.user_id, b.resource_id, r.name AS resource_name, "
                    + "b.booking_date, b.start_time, b.end_time, b.status, b.created_at "
                    + "FROM bookings b JOIN resources r ON r.id = b.resource_id ";

    /** Start times already booked (not cancelled) for one resource on one date. */
    public List<LocalTime> findBookedStartTimes(int resourceId, LocalDate date) throws SQLException {
        String sql = "SELECT start_time FROM bookings "
                + "WHERE resource_id = ? AND booking_date = ? AND status <> 'CANCELLED'";

        List<LocalTime> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resourceId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getTime("start_time").toLocalTime());
                }
            }
        }
        return result;
    }

    /** Inserts a CONFIRMED booking. Throws SQLException (duplicate key) if the slot is already taken. */
    public int insert(int userId, int resourceId, LocalDate date, LocalTime start, LocalTime end)
            throws SQLException {

        String sql = "INSERT INTO bookings (user_id, resource_id, booking_date, start_time, end_time, status) "
                + "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(start));
            ps.setTime(5, Time.valueOf(end));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<Booking> findByUser(int userId) throws SQLException {
        String sql = SELECT_WITH_RESOURCE + "WHERE b.user_id = ? ORDER BY b.booking_date DESC, b.start_time DESC";

        List<Booking> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Cancels a booking only if it belongs to this user (ownership double-checked here too). */
    public boolean cancel(int bookingId, int userId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED' "
                + "WHERE id = ? AND user_id = ? AND status <> 'CANCELLED'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countUpcomingByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND status <> 'CANCELLED' "
                + "AND (booking_date > CURDATE() OR (booking_date = CURDATE() AND start_time >= CURTIME()))";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countActiveByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND status = 'CONFIRMED'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("resource_id"),
                rs.getString("resource_name"),
                rs.getDate("booking_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
    public int countAllActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status <> 'CANCELLED'";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Lightweight rows for the admin dashboard's recent-bookings list. */
    public List<BookingSummary> findRecentForAdmin(int limit) throws SQLException {
        String sql = "SELECT b.id, r.name AS resource_name, u.full_name AS student_name, "
                + "b.booking_date, b.start_time, b.end_time, b.status "
                + "FROM bookings b "
                + "JOIN resources r ON r.id = b.resource_id "
                + "JOIN users u ON u.id = b.user_id "
                + "ORDER BY b.created_at DESC LIMIT ?";

        List<BookingSummary> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new BookingSummary(
                            rs.getInt("id"),
                            rs.getString("resource_name"),
                            rs.getString("student_name"),
                            rs.getDate("booking_date").toLocalDate(),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            BookingStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        }
        return result;
    }

    public static class BookingSummary {
        private final int id;
        private final String resourceName;
        private final String studentName;
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final BookingStatus status;

        public BookingSummary(int id, String resourceName, String studentName, LocalDate date,
                              LocalTime startTime, LocalTime endTime, BookingStatus status) {
            this.id = id;
            this.resourceName = resourceName;
            this.studentName = studentName;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }

        public int getId()               { return id; }
        public String getResourceName()  { return resourceName; }
        public String getStudentName()   { return studentName; }
        public LocalDate getDate()       { return date; }
        public LocalTime getStartTime()  { return startTime; }
        public LocalTime getEndTime()    { return endTime; }
        public BookingStatus getStatus() { return status; }
    }
    /** Marks past CONFIRMED bookings as COMPLETED. Safe to call often — it's a simple UPDATE. */
    public void completePastBookings() throws SQLException {
        String sql = "UPDATE bookings SET status = 'COMPLETED' "
                + "WHERE status = 'CONFIRMED' "
                + "AND (booking_date < CURDATE() "
                + "     OR (booking_date = CURDATE() AND end_time <= CURTIME()))";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }
}