package com.skillswap.dao;

import com.skillswap.model.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {

    public boolean createRating(Rating rating) {
        // Check if this user has already rated this booking
        String checkSql = "SELECT COUNT(*) FROM ratings WHERE booking_id = ? AND rated_by_learner = ?";
        String sql = "INSERT INTO ratings (booking_id, rating, review, rated_by_learner) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Check if already rated by this role
            checkPs.setInt(1, rating.getBookingId());
            checkPs.setBoolean(2, rating.isRatedByLearner());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Already rated by this role
                }
            }
            
            // Insert new rating
            ps.setInt(1, rating.getBookingId());
            ps.setInt(2, rating.getRating());
            ps.setString(3, rating.getReview());
            ps.setBoolean(4, rating.isRatedByLearner());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasRated(int bookingId, boolean isLearner) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE booking_id = ? AND rated_by_learner = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setBoolean(2, isLearner);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Rating> getRatingsByUser(int userId) {
        List<Rating> list = new ArrayList<>();
        String sql = "SELECT r.* FROM ratings r " +
                     "JOIN bookings b ON r.booking_id = b.id " +
                     "JOIN sessions s ON b.session_id = s.id " +
                     "WHERE s.tutor_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rating r = new Rating();
                    r.setId(rs.getInt("id"));
                    r.setBookingId(rs.getInt("booking_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setReview(rs.getString("review"));
                    r.setCreatedAt(rs.getString("created_at"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getAverageRating(int userId) {
        String sql = "SELECT COALESCE(AVG(CAST(r.rating AS DOUBLE)), 0.0) FROM ratings r " +
                     "JOIN bookings b ON r.booking_id = b.id " +
                     "JOIN sessions s ON b.session_id = s.id " +
                     "WHERE s.tutor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Object[]> getLeaderboard(int limit) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT u.name, u.skills, " +
                     "COALESCE(AVG(CAST(r.rating AS DOUBLE)), 0.0) AS avg_rating, " +
                     "COUNT(DISTINCT s.id) AS total_sessions, " +
                     "COUNT(DISTINCT r.id) AS total_reviews " +
                     "FROM users u " +
                     "LEFT JOIN sessions s ON u.id = s.tutor_id " +
                     "LEFT JOIN bookings b ON s.id = b.session_id " +
                     "LEFT JOIN ratings r ON b.id = r.booking_id " +
                     "GROUP BY u.id, u.name, u.skills " +
                     "ORDER BY avg_rating DESC, total_reviews DESC, total_sessions DESC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    rows.add(new Object[]{
                        "#" + (rank++),
                        rs.getString("name"),
                        rs.getString("skills"),
                        avg > 0 ? String.format("%.1f / 5", avg) : "No ratings",
                        rs.getInt("total_sessions"),
                        rs.getInt("total_reviews")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
