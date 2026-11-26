package com.skillswap.dao;

import com.skillswap.model.ExchangeRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRequestDAO {

    public boolean createRequest(ExchangeRequest request) {
        String sql = "INSERT INTO exchange_requests (from_user_id, to_user_id, skill_offering, skill_requesting, message, status) VALUES (?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, request.getFromUserId());
            ps.setInt(2, request.getToUserId());
            ps.setString(3, request.getSkillOffering());
            ps.setString(4, request.getSkillRequesting());
            ps.setString(5, request.getMessage());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ExchangeRequest> getIncomingRequests(int userId) {
        List<ExchangeRequest> list = new ArrayList<>();
        String sql = "SELECT er.*, u.name AS from_name FROM exchange_requests er " +
                     "JOIN users u ON er.from_user_id = u.id " +
                     "WHERE er.to_user_id = ? ORDER BY er.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExchangeRequest er = mapRow(rs);
                    er.setFromUserName(rs.getString("from_name"));
                    list.add(er);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ExchangeRequest> getSentRequests(int userId) {
        List<ExchangeRequest> list = new ArrayList<>();
        String sql = "SELECT er.*, u.name AS to_name FROM exchange_requests er " +
                     "JOIN users u ON er.to_user_id = u.id " +
                     "WHERE er.from_user_id = ? ORDER BY er.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExchangeRequest er = mapRow(rs);
                    er.setToUserName(rs.getString("to_name"));
                    list.add(er);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateRequestStatus(int requestId, String status) {
        if (!"accepted".equals(status) && !"rejected".equals(status)) return false;
        String sql = "UPDATE exchange_requests SET status = ? WHERE id = ? AND status = 'pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ExchangeRequest mapRow(ResultSet rs) throws SQLException {
        ExchangeRequest er = new ExchangeRequest();
        er.setId(rs.getInt("id"));
        er.setFromUserId(rs.getInt("from_user_id"));
        er.setToUserId(rs.getInt("to_user_id"));
        er.setSkillOffering(rs.getString("skill_offering"));
        er.setSkillRequesting(rs.getString("skill_requesting"));
        er.setMessage(rs.getString("message"));
        er.setStatus(rs.getString("status"));
        er.setCreatedAt(rs.getString("created_at"));
        return er;
    }
}
