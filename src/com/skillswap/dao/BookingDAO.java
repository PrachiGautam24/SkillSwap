package com.skillswap.dao;

import com.skillswap.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean createBooking(int sessionId, int learnerId) {
        String lockSql = "SELECT id FROM sessions WHERE id = ? AND tutor_id <> ? " +
                         "AND status = 'open' AND session_type = 'offering' FOR UPDATE";
        String insertSql = "INSERT INTO bookings (session_id, learner_id, status) " +
                           "SELECT ?, ?, 'pending' WHERE NOT EXISTS (SELECT 1 FROM bookings " +
                           "WHERE session_id = ? AND learner_id = ? AND status <> 'cancelled')";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lock = conn.prepareStatement(lockSql);
                 PreparedStatement insert = conn.prepareStatement(insertSql)) {
                lock.setInt(1, sessionId);
                lock.setInt(2, learnerId);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
                insert.setInt(1, sessionId);
                insert.setInt(2, learnerId);
                insert.setInt(3, sessionId);
                insert.setInt(4, learnerId);
                boolean created = insert.executeUpdate() == 1;
                conn.commit();
                return created;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Booking> getBookingsBySession(int sessionId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.name AS learner_name, u.email AS learner_email " +
                     "FROM bookings b JOIN users u ON b.learner_id = u.id " +
                     "WHERE b.session_id = ? ORDER BY b.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setSessionId(rs.getInt("session_id"));
                    b.setLearnerId(rs.getInt("learner_id"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getString("created_at"));
                    b.setLearnerName(rs.getString("learner_name"));
                    b.setLearnerEmail(rs.getString("learner_email"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, s.title AS session_title, s.skill, s.scheduled_at, " +
                     "t.name AS tutor_name, l.name AS learner_name, COALESCE(r.rating, 0) AS rating_val " +
                     "FROM bookings b " +
                     "JOIN sessions s ON b.session_id = s.id " +
                     "JOIN users t ON s.tutor_id = t.id " +
                     "JOIN users l ON b.learner_id = l.id " +
                     "LEFT JOIN ratings r ON b.id = r.booking_id " +
                     "WHERE (b.learner_id = ? OR s.tutor_id = ?) " +
                     "ORDER BY b.created_at DESC, b.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setSessionId(rs.getInt("session_id"));
                    b.setLearnerId(rs.getInt("learner_id"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getString("created_at"));
                    b.setScheduledAt(rs.getString("scheduled_at"));
                    b.setSessionTitle(rs.getString("session_title"));
                    b.setSkill(rs.getString("skill"));
                    b.setTutorName(rs.getString("tutor_name"));
                    b.setLearnerName(rs.getString("learner_name"));
                    b.setRatingValue(rs.getInt("rating_val"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String eligible;
        switch (status) {
            case "confirmed": eligible = "status = 'pending'"; break;
            case "completed": eligible = "status = 'confirmed'"; break;
            case "cancelled": eligible = "status IN ('pending', 'confirmed')"; break;
            default: return false;
        }
        String sql = "UPDATE bookings SET status = ? WHERE id = ? AND " + eligible;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
