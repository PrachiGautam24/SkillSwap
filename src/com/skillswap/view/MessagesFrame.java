package com.skillswap.view;

import com.skillswap.dao.*;
import com.skillswap.model.*;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * In-app messaging interface for safe peer communication
 * No personal contact information exposed
 */
public class MessagesFrame extends JFrame {
    private final User currentUser;
    private JList<String> conversationList;
    private DefaultListModel<String> conversationModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel chatHeaderLabel;
    private JLabel unreadBadge;
    
    private List<Object[]> conversations;
    private int selectedOtherUserId = -1;
    private String selectedOtherUserName = "";
    
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    
    public MessagesFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("SkillSwap - Messages");
        setSize(900, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        SwingUtils.centerFrame(this);
        setLayout(new BorderLayout());
        
        setupUI();
        loadConversations();
    }
    
    private void setupUI() {
        // Left panel - conversation list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(SwingUtils.CARD);
        leftPanel.setPreferredSize(new Dimension(280, getHeight()));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, SwingUtils.BORDER_COLOR));
        
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(SwingUtils.CARD);
        leftHeader.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel messagesTitle = SwingUtils.createLabel("Messages", SwingUtils.SUBTITLE_FONT, SwingUtils.TEXT_PRIMARY);
        unreadBadge = SwingUtils.createLabel("", SwingUtils.BODY_FONT, Color.WHITE);
        unreadBadge.setOpaque(true);
        unreadBadge.setBackground(SwingUtils.DANGER);
        unreadBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
        leftHeader.add(messagesTitle, BorderLayout.WEST);
        leftHeader.add(unreadBadge, BorderLayout.EAST);
        
        conversationModel = new DefaultListModel<>();
        conversationList = new JList<>(conversationModel);
        conversationList.setBackground(SwingUtils.CARD);
        conversationList.setForeground(SwingUtils.TEXT_PRIMARY);
        conversationList.setFont(SwingUtils.BODY_FONT);
        conversationList.setSelectionBackground(SwingUtils.PRIMARY_DARK);
        conversationList.setSelectionForeground(Color.WHITE);
        conversationList.setBorder(new EmptyBorder(5, 10, 5, 10));
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && conversationList.getSelectedIndex() >= 0) {
                loadSelectedConversation();
            }
        });
        
        JScrollPane conversationScroll = new JScrollPane(conversationList);
        conversationScroll.setBorder(BorderFactory.createEmptyBorder());
        
        JButton newMessageBtn = SwingUtils.createStyledButton("+ New Message", SwingUtils.PRIMARY_DARK);
        newMessageBtn.addActionListener(e -> openNewMessageDialog());
        JPanel leftFooter = new JPanel(new BorderLayout());
        leftFooter.setBackground(SwingUtils.CARD);
        leftFooter.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftFooter.add(newMessageBtn, BorderLayout.CENTER);
        
        leftPanel.add(leftHeader, BorderLayout.NORTH);
        leftPanel.add(conversationScroll, BorderLayout.CENTER);
        leftPanel.add(leftFooter, BorderLayout.SOUTH);
        
        // Right panel - chat area
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(SwingUtils.BACKGROUND);
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(SwingUtils.CARD);
        chatHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SwingUtils.BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        chatHeaderLabel = SwingUtils.createLabel("Select a conversation", SwingUtils.SUBTITLE_FONT, SwingUtils.TEXT_PRIMARY);
        JButton viewProfileBtn = SwingUtils.createStyledButton("View Profile", SwingUtils.CARD);
        viewProfileBtn.addActionListener(e -> viewOtherUserProfile());
        chatHeader.add(chatHeaderLabel, BorderLayout.WEST);
        chatHeader.add(viewProfileBtn, BorderLayout.EAST);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(SwingUtils.BODY_FONT);
        chatArea.setBackground(SwingUtils.BACKGROUND);
        chatArea.setForeground(SwingUtils.TEXT_PRIMARY);
        chatArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(SwingUtils.CARD);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, SwingUtils.BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        messageField = SwingUtils.createStyledTextField(30);
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());
        
        sendButton = SwingUtils.createStyledButton("Send", SwingUtils.PRIMARY_DARK);
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        rightPanel.add(chatHeader, BorderLayout.NORTH);
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }
    
    public void loadConversations() {
        conversationModel.clear();
        conversations = messageDAO.getConversationList(currentUser.getId());
        
        int totalUnread = 0;
        for (Object[] conv : conversations) {
            String userName = (String) conv[1];
            int unreadCount = (int) conv[3];
            totalUnread += unreadCount;
            
            String display = userName;
            if (unreadCount > 0) {
                display += " (" + unreadCount + " new)";
            }
            conversationModel.addElement(display);
        }
        
        if (totalUnread > 0) {
            unreadBadge.setText(String.valueOf(totalUnread));
            unreadBadge.setVisible(true);
        } else {
            unreadBadge.setVisible(false);
        }
        
        if (conversations.isEmpty()) {
            chatArea.setText("You have no messages yet.\n\nStart a conversation by:\n" +
                "1. Sending an exchange request to a peer\n" +
                "2. Or clicking '+ New Message' to message someone you've connected with");
        }
    }
    
    private void loadSelectedConversation() {
        int index = conversationList.getSelectedIndex();
        if (index < 0 || index >= conversations.size()) return;
        
        Object[] conv = conversations.get(index);
        selectedOtherUserId = (int) conv[0];
        selectedOtherUserName = (String) conv[1];
        
        chatHeaderLabel.setText("Chat with " + selectedOtherUserName);
        messageField.setEnabled(true);
        sendButton.setEnabled(true);
        
        // Mark messages as read
        messageDAO.markAsRead(currentUser.getId(), selectedOtherUserId);
        
        // Load conversation
        List<Message> messages = messageDAO.getConversation(currentUser.getId(), selectedOtherUserId);
        StringBuilder sb = new StringBuilder();
        
        for (Message msg : messages) {
            boolean isSentByMe = msg.getSenderId() == currentUser.getId();
            String sender = isSentByMe ? "You" : msg.getSenderName();
            String time = msg.getCreatedAt().substring(0, Math.min(16, msg.getCreatedAt().length()));
            
            sb.append("[").append(time).append("] ").append(sender).append(":\n");
            sb.append(msg.getContent()).append("\n\n");
        }
        
        chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        
        // Reload conversation list to update unread counts
        SwingUtilities.invokeLater(this::loadConversations);
    }
    
    private void sendMessage() {
        if (selectedOtherUserId < 0) return;
        
        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            SwingUtils.showError(this, "Message cannot be empty");
            return;
        }
        
        if (content.length() > 1000) {
            SwingUtils.showError(this, "Message must be less than 1000 characters");
            return;
        }
        
        Message msg = new Message();
        msg.setSenderId(currentUser.getId());
        msg.setReceiverId(selectedOtherUserId);
        msg.setContent(content);
        
        if (messageDAO.sendMessage(msg)) {
            messageField.setText("");
            loadSelectedConversation();
        } else {
            SwingUtils.showError(this, "Failed to send message. Please try again.");
        }
    }
    
    private void openNewMessageDialog() {
        // Show list of users the current user has interacted with (bookings or exchange requests)
        List<User> allUsers = userDAO.getAllUsers();
        List<User> availableUsers = new ArrayList<>();
        
        for (User u : allUsers) {
            if (u.getId() != currentUser.getId()) {
                availableUsers.add(u);
            }
        }
        
        if (availableUsers.isEmpty()) {
            SwingUtils.showError(this, "No users available to message. Connect with peers first!");
            return;
        }
        
        String[] userNames = new String[availableUsers.size()];
        for (int i = 0; i < availableUsers.size(); i++) {
            userNames[i] = availableUsers.get(i).getName();
        }
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select a user to message:",
            "New Message",
            JOptionPane.QUESTION_MESSAGE,
            null,
            userNames,
            userNames[0]
        );
        
        if (selected != null) {
            for (int i = 0; i < userNames.length; i++) {
                if (userNames[i].equals(selected)) {
                    selectedOtherUserId = availableUsers.get(i).getId();
                    selectedOtherUserName = availableUsers.get(i).getName();
                    
                    // Create a new conversation entry if it doesn't exist
                    Message initialMsg = new Message();
                    initialMsg.setSenderId(currentUser.getId());
                    initialMsg.setReceiverId(selectedOtherUserId);
                    initialMsg.setContent("Hi " + selectedOtherUserName + "!");
                    
                    if (messageDAO.sendMessage(initialMsg)) {
                        loadConversations();
                        // Select the new conversation
                        for (int j = 0; j < conversations.size(); j++) {
                            if ((int)conversations.get(j)[0] == selectedOtherUserId) {
                                conversationList.setSelectedIndex(j);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private void viewOtherUserProfile() {
        if (selectedOtherUserId < 0) {
            SwingUtils.showError(this, "Please select a conversation first");
            return;
        }
        
        User otherUser = userDAO.getById(selectedOtherUserId);
        if (otherUser == null) return;
        
        JDialog dialog = new JDialog(this, otherUser.getName() + "'s Profile", true);
        FormPage page = new FormPage(otherUser.getName());
        page.section("Skill exchange");
        page.detail("Can teach", otherUser.getSkills());
        page.detail("Wants to learn", otherUser.getSkillsToLearn());
        page.detail("Learning mode", otherUser.getLearningMode());
        page.detail("Preferred time", otherUser.getPreferredTime());
        page.section("Student details");
        page.detail("Department", otherUser.getDepartment());
        page.detail("Semester", otherUser.getSemester());
        page.detail("Bio", otherUser.getBio());
        
        JButton close = SwingUtils.createStyledButton("Close", SwingUtils.CARD);
        close.addActionListener(e -> dialog.dispose());
        page.actions(close);
        dialog.setContentPane(page);
        dialog.getRootPane().setDefaultButton(close);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 500);
        SwingUtils.centerFrame(dialog);
        dialog.setVisible(true);
    }
}
