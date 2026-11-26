package com.skillswap.dao;

import com.skillswap.model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for in-app messaging
 * Provides safe communication without exposing personal contact info
 */
public class MessageDAO {
    
    /**
     * Send a message between two users
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, exchange_request_id, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            if (message.getExchangeRequestId() > 0) {
                ps.setInt(3, message.getExchangeRequestId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, message.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get conversation between two users
     */
    public List<Message> getConversation(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "s.name as sender_name, r.name as receiver_name " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.id " +
                     "JOIN users r ON m.receiver_id = r.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.created_at ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    /**
     * Get all conversations for a user (list of users they've messaged with)
     */
    public List<Object[]> getConversationList(int userId) {
        List<Object[]> conversations = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                     "CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END as other_user_id, " +
                     "CASE WHEN m.sender_id = ? THEN r.name ELSE s.name END as other_user_name, " +
                     "MAX(m.created_at) as last_message_time, " +
                     "SUM(CASE WHEN m.receiver_id = ? AND m.is_read = FALSE THEN 1 ELSE 0 END) as unread_count " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.id " +
                     "JOIN users r ON m.receiver_id = r.id " +
                     "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                     "GROUP BY other_user_id, other_user_name " +
                     "ORDER BY last_message_time DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] conv = new Object[4];
                    conv[0] = rs.getInt("other_user_id");
                    conv[1] = rs.getString("other_user_name");
                    conv[2] = rs.getString("last_message_time");
                    conv[3] = rs.getInt("unread_count");
                    conversations.add(conv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }
    
    /**
     * Mark messages as read
     */
    public boolean markAsRead(int receiverId, int senderId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND sender_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ps.setInt(2, senderId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get unread message count for a user
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get messages related to a specific exchange request
     */
    public List<Message> getMessagesByExchangeRequest(int exchangeRequestId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.name as sender_name, r.name as receiver_name " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.id " +
                     "JOIN users r ON m.receiver_id = r.id " +
                     "WHERE m.exchange_request_id = ? " +
                     "ORDER BY m.created_at ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeRequestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setExchangeRequestId(rs.getInt("exchange_request_id"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getString("created_at"));
        m.setRead(rs.getBoolean("is_read"));
        m.setSenderName(rs.getString("sender_name"));
        m.setReceiverName(rs.getString("receiver_name"));
        return m;
    }
}
