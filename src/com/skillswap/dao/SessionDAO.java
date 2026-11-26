package com.skillswap.dao;

import com.skillswap.model.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public boolean createSession(Session session) {
        String sql = "INSERT INTO sessions (tutor_id, title, description, skill, scheduled_at, status, session_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, session.getTutorId());
            ps.setString(2, session.getTitle());
            ps.setString(3, session.getDescription());
            ps.setString(4, session.getSkill());
            ps.setString(5, session.getScheduledAt());
            ps.setString(6, session.getStatus() != null ? session.getStatus() : "open");
            ps.setString(7, session.getSessionType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Session> getSessionsByUser(int userId, String sessionType) {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, (SELECT COUNT(*) FROM bookings b WHERE b.session_id = s.id) AS b_count " +
                     "FROM sessions s WHERE s.tutor_id = ? AND s.session_type = ? ORDER BY s.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, sessionType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Session s = mapRow(rs);
                    s.setBookingCount(rs.getInt("b_count"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Session> getAllOpenSessions() {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name AS tutor_name FROM sessions s " +
                     "JOIN users u ON s.tutor_id = u.id WHERE s.status = 'open' ORDER BY s.id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Session s = mapRow(rs);
                s.setTutorName(rs.getString("tutor_name"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Session> getBookableSessions(int hostId, int learnerId) throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT s.* FROM sessions s WHERE s.tutor_id = ? AND s.tutor_id <> ? " +
                     "AND s.session_type = 'offering' AND s.status = 'open' " +
                     "AND NOT EXISTS (SELECT 1 FROM bookings b WHERE b.session_id = s.id " +
                     "AND b.learner_id = ? AND b.status <> 'cancelled') ORDER BY s.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hostId);
            ps.setInt(2, learnerId);
            ps.setInt(3, learnerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) sessions.add(mapRow(rs));
            }
        }
        return sessions;
    }

    public boolean updateSession(Session session) {
        String sql = "UPDATE sessions SET title=?, description=?, skill=?, scheduled_at=?, session_type=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, session.getTitle());
            ps.setString(2, session.getDescription());
            ps.setString(3, session.getSkill());
            ps.setString(4, session.getScheduledAt());
            ps.setString(5, session.getSessionType());
            ps.setInt(6, session.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSession(int sessionId) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setId(rs.getInt("id"));
        s.setTutorId(rs.getInt("tutor_id"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setSkill(rs.getString("skill"));
        s.setScheduledAt(rs.getString("scheduled_at"));
        s.setStatus(rs.getString("status"));
        s.setSessionType(rs.getString("session_type"));
        return s;
    }
}
