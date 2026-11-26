package com.skillswap.view;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.dao.MessageDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Message;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyRequestsFrame extends JFrame {
    private final User currentUser;
    private final JTabbedPane tabs = new JTabbedPane();
    private JTable incomingTable, sentTable;
    private List<ExchangeRequest> incomingList, sentList;
    private final ExchangeRequestDAO requestDAO = new ExchangeRequestDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();

    public MyRequestsFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("SkillSwap - My Exchange Requests");
        setSize(880, 500);
        SwingUtils.centerFrame(this);

        setContentPane(SwingUtils.page());
        add(SwingUtils.heading("Exchange requests", null), BorderLayout.NORTH);
        tabs.setFont(SwingUtils.BOLD_FONT);

        JPanel incomingPanel = new JPanel(new BorderLayout(10, 10));
        incomingPanel.setBackground(SwingUtils.BACKGROUND);
        incomingTable = SwingUtils.createTable("No incoming exchange requests.");
        incomingPanel.add(new JScrollPane(incomingTable), BorderLayout.CENTER);

        JPanel inBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        inBtns.setBackground(SwingUtils.CARD);
        JButton messageBtn = SwingUtils.createStyledButton("Message Sender", SwingUtils.CARD);
        JButton acceptBtn = SwingUtils.createStyledButton("Accept", SwingUtils.SUCCESS);
        JButton rejectBtn = SwingUtils.createStyledButton("Reject", SwingUtils.DANGER);

        messageBtn.addActionListener(e -> messageSelectedUser(true));
        acceptBtn.addActionListener(e -> updateSelectedStatus("accepted"));
        rejectBtn.addActionListener(e -> updateSelectedStatus("rejected"));
        messageBtn.setEnabled(false);
        acceptBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        incomingTable.getSelectionModel().addListSelectionListener(e -> {
            int selected = incomingTable.getSelectedRow();
            boolean validSelection = selected >= 0 && selected < incomingList.size();
            messageBtn.setEnabled(validSelection);
            boolean pending = validSelection && "pending".equals(incomingList.get(selected).getStatus());
            acceptBtn.setEnabled(pending);
            rejectBtn.setEnabled(pending);
        });
        inBtns.add(messageBtn);
        inBtns.add(acceptBtn);
        inBtns.add(rejectBtn);
        incomingPanel.add(inBtns, BorderLayout.SOUTH);

        JPanel sentPanel = new JPanel(new BorderLayout());
        sentPanel.setBackground(SwingUtils.BACKGROUND);
        sentTable = SwingUtils.createTable("Requests you send from Find Matches will appear here.");
        sentPanel.add(new JScrollPane(sentTable), BorderLayout.CENTER);
        
        JPanel sentBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        sentBtns.setBackground(SwingUtils.CARD);
        JButton messageSentBtn = SwingUtils.createStyledButton("Message Recipient", SwingUtils.CARD);
        messageSentBtn.setEnabled(false);
        messageSentBtn.addActionListener(e -> messageSelectedUser(false));
        sentTable.getSelectionModel().addListSelectionListener(e -> {
            messageSentBtn.setEnabled(sentTable.getSelectedRow() >= 0);
        });
        sentBtns.add(messageSentBtn);
        sentPanel.add(sentBtns, BorderLayout.SOUTH);

        tabs.addTab("Incoming Requests", incomingPanel);
        tabs.addTab("Sent Requests", sentPanel);
        add(tabs);

        loadTables();
    }

    void loadTables() {
        incomingList = requestDAO.getIncomingRequests(currentUser.getId());
        DefaultTableModel inModel = new DefaultTableModel(
                new String[]{"From Student", "They Offer", "They Request", "Message", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ExchangeRequest r : incomingList) {
            inModel.addRow(new Object[]{r.getFromUserName(), r.getSkillOffering(), r.getSkillRequesting(), r.getMessage(), r.getStatus().toUpperCase()});
        }
        incomingTable.setModel(inModel);

        sentList = requestDAO.getSentRequests(currentUser.getId());
        DefaultTableModel sentModel = new DefaultTableModel(
                new String[]{"To Student", "You Offered", "You Requested", "Message", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ExchangeRequest r : sentList) {
            sentModel.addRow(new Object[]{r.getToUserName(), r.getSkillOffering(), r.getSkillRequesting(), r.getMessage(), r.getStatus().toUpperCase()});
        }
        sentTable.setModel(sentModel);
        tabs.setTitleAt(0, "Incoming (" + incomingList.size() + ")");
        tabs.setTitleAt(1, "Sent (" + sentList.size() + ")");
    }

    private void updateSelectedStatus(String status) {
        int row = incomingTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Select an incoming request first.");
            return;
        }
        ExchangeRequest req = incomingList.get(row);
        
        if (requestDAO.updateRequestStatus(req.getId(), status)) {
            if ("accepted".equals(status)) {
                // Automatically send a notification message
                Message notification = new Message();
                notification.setSenderId(currentUser.getId());
                notification.setReceiverId(req.getFromUserId());
                notification.setExchangeRequestId(req.getId());
                notification.setContent("Great news! I've accepted your exchange request. Let's discuss when we can meet!");
                messageDAO.sendMessage(notification);
                
                SwingUtils.showSuccess(this, "Request accepted! A message has been sent. Check Messages to coordinate.");
            } else {
                SwingUtils.showSuccess(this, "Request marked as " + status + ".");
            }
            loadTables();
        } else {
            loadTables();
            SwingUtils.showError(this, "Could not change this request. Check its current status.");
        }
    }
    
    private void messageSelectedUser(boolean isIncoming) {
        JTable table = isIncoming ? incomingTable : sentTable;
        List<ExchangeRequest> list = isIncoming ? incomingList : sentList;
        
        int row = table.getSelectedRow();
        if (row < 0 || row >= list.size()) {
            SwingUtils.showError(this, "Please select a request first.");
            return;
        }
        
        ExchangeRequest req = list.get(row);
        int otherUserId = isIncoming ? req.getFromUserId() : req.getToUserId();
        User otherUser = userDAO.getById(otherUserId);
        
        if (otherUser == null) {
            SwingUtils.showError(this, "Could not load user information.");
            return;
        }
        
        // Open quick message dialog
        JDialog dialog = new JDialog(this, "Message " + otherUser.getName(), true);
        FormPage page = new FormPage("Send a message");
        
        JTextArea messageArea = SwingUtils.createStyledTextArea(5, 30);
        messageArea.setText("Hi " + otherUser.getName() + ",\n\nRegarding the exchange request for " + 
                          req.getSkillOffering() + " ↔ " + req.getSkillRequesting() + "...");
        
        page.section("About this exchange");
        page.detail("Peer", otherUser.getName());
        page.detail("Exchange", req.getSkillOffering() + " ↔ " + req.getSkillRequesting());
        page.detail("Status", req.getStatus().toUpperCase());
        page.field("Your message", new JScrollPane(messageArea));
        
        JButton cancel = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton send = SwingUtils.createStyledButton("Send Message", SwingUtils.PRIMARY_DARK);
        JButton openChat = SwingUtils.createStyledButton("Open Full Chat", SwingUtils.SUCCESS);
        
        cancel.addActionListener(e -> dialog.dispose());
        send.addActionListener(e -> {
            String content = messageArea.getText().trim();
            if (content.isEmpty()) {
                SwingUtils.showError(dialog, "Message cannot be empty");
                return;
            }
            
            Message msg = new Message();
            msg.setSenderId(currentUser.getId());
            msg.setReceiverId(otherUserId);
            msg.setExchangeRequestId(req.getId());
            msg.setContent(content);
            
            if (messageDAO.sendMessage(msg)) {
                SwingUtils.showSuccess(dialog, "Message sent to " + otherUser.getName() + "!");
                dialog.dispose();
            } else {
                SwingUtils.showError(dialog, "Failed to send message. Please try again.");
            }
        });
        
        openChat.addActionListener(e -> {
            dialog.dispose();
            MessagesFrame messagesFrame = new MessagesFrame(currentUser);
            messagesFrame.setVisible(true);
        });
        
        page.actions(cancel, send, openChat);
        dialog.setContentPane(page);
        dialog.getRootPane().setDefaultButton(send);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 500);
        SwingUtils.centerFrame(dialog);
        dialog.setVisible(true);
    }
}
