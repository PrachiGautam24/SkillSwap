package com.skillswap.model;

/**
 * Message model for in-app communication between peers
 * Privacy-first: no personal contact info exposed
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private int exchangeRequestId; // Optional: link to exchange request
    private String content;
    private String createdAt;
    private boolean isRead;
    
    // Joined fields for display
    private String senderName;
    private String receiverName;
    
    public Message() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public int getExchangeRequestId() { return exchangeRequestId; }
    public void setExchangeRequestId(int exchangeRequestId) { this.exchangeRequestId = exchangeRequestId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
}
