package com.skillswap.view;

import com.skillswap.dao.BookingDAO;
import com.skillswap.dao.MessageDAO;
import com.skillswap.dao.SessionDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.Message;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class MySessionsFrame extends JFrame {
    private final User currentUser;
    private final Runnable onChange;
    private final SessionDAO sessionDAO = new SessionDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTable bookingTable = SwingUtils.createTable("No bookings match this view.");
    private final JTable offeringTable = SwingUtils.createTable("Your teaching offers will appear here.");
    private final JTable requestingTable = SwingUtils.createTable("Your requests for help will appear here.");
    private final JComboBox<String> roleFilter = new JComboBox<>(new String[]{"All bookings", "As learner", "As host"});
    private final JLabel summary = SwingUtils.createLabel("", SwingUtils.BODY_FONT, SwingUtils.TEXT_SECONDARY);
    private final JPanel hostActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JPanel bookingActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JButton manage = SwingUtils.createStyledButton("View bookings", SwingUtils.PRIMARY_DARK);
    private final JButton edit = SwingUtils.createStyledButton("Edit", SwingUtils.CARD);
    private final JButton delete = SwingUtils.createStyledButton("Delete", SwingUtils.DANGER);
    
    // Booking action buttons
    private final JButton acceptBooking = SwingUtils.createStyledButton("Accept", SwingUtils.SUCCESS);
    private final JButton rejectBooking = SwingUtils.createStyledButton("Reject", SwingUtils.DANGER);
    private final JButton messagePartner = SwingUtils.createStyledButton("Message Partner", SwingUtils.PRIMARY_DARK);
    private final JButton completeBooking = SwingUtils.createStyledButton("Mark as Completed", SwingUtils.SUCCESS);
    private final JButton cancelBooking = SwingUtils.createStyledButton("Cancel Booking", SwingUtils.DANGER);
    private List<Session> offerings = Collections.emptyList(), requests = Collections.emptyList();
    private List<Booking> bookings = Collections.emptyList();

    public MySessionsFrame(User user) { this(user, null); }

    MySessionsFrame(User user, Runnable onChange) {
        currentUser = user;
        this.onChange = onChange;
        setTitle("SkillSwap - My Sessions");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 540);
        SwingUtils.centerFrame(this);

        JPanel page = SwingUtils.page();
        setContentPane(page);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tools.setOpaque(false);
        JButton refresh = SwingUtils.createStyledButton("Refresh", SwingUtils.CARD);
        JButton create = SwingUtils.createStyledButton("+ Create session", SwingUtils.PRIMARY_DARK);
        refresh.addActionListener(e -> loadSessions());
        create.addActionListener(e -> openEditor(null));
        tools.add(refresh);
        tools.add(create);
        page.add(SwingUtils.heading("My Sessions", tools), BorderLayout.NORTH);

        tabs.setFont(SwingUtils.BOLD_FONT);
        tabs.setBackground(SwingUtils.CARD);
        tabs.setForeground(SwingUtils.TEXT_PRIMARY);
        tabs.addTab("Bookings", new JScrollPane(bookingTable));
        tabs.addTab("My offers", new JScrollPane(offeringTable));
        tabs.addTab("My help requests", new JScrollPane(requestingTable));
        page.add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        footer.add(summary, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        
        // Role filter for bookings view
        roleFilter.getAccessibleContext().setAccessibleName("Filter bookings by your role");
        roleFilter.addActionListener(e -> showBookings());
        actions.add(roleFilter);
        
        // Session management actions (for My offers / My help requests tabs)
        hostActions.setOpaque(false);
        manage.addActionListener(e -> {
            Session s = selectedPost();
            if (s != null) new SessionBookingsDialog(this, s, this::sessionChanged).setVisible(true);
        });
        edit.addActionListener(e -> {
            Session s = selectedPost();
            if (s != null) openEditor(s);
        });
        delete.addActionListener(e -> deletePost());
        hostActions.add(manage);
        hostActions.add(edit);
        hostActions.add(delete);
        actions.add(hostActions);
        
        // Booking actions (for Bookings tab)
        bookingActions.setOpaque(false);
        acceptBooking.addActionListener(e -> handleBookingAction("confirmed"));
        rejectBooking.addActionListener(e -> handleBookingAction("cancelled"));
        completeBooking.addActionListener(e -> handleBookingAction("completed"));
        cancelBooking.addActionListener(e -> handleBookingAction("cancelled"));
        messagePartner.addActionListener(e -> messageBookingPartner());
        bookingActions.add(messagePartner);
        bookingActions.add(acceptBooking);
        bookingActions.add(rejectBooking);
        bookingActions.add(completeBooking);
        bookingActions.add(cancelBooking);
        actions.add(bookingActions);
        
        footer.add(actions, BorderLayout.EAST);
        page.add(footer, BorderLayout.SOUTH);

        tabs.addChangeListener(e -> updateActions());
        bookingTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        offeringTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        requestingTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        loadSessions();
    }

    void loadSessions() {
        int id = currentUser.getId();
        bookings = bookingDAO.getBookingsByUser(id);
        offerings = sessionDAO.getSessionsByUser(id, "offering");
        requests = sessionDAO.getSessionsByUser(id, "requesting");
        offeringTable.setModel(postModel(offerings));
        requestingTable.setModel(postModel(requests));
        tabs.setTitleAt(0, "Bookings (" + bookings.size() + ")");
        tabs.setTitleAt(1, "My offers (" + offerings.size() + ")");
        tabs.setTitleAt(2, "My help requests (" + requests.size() + ")");
        showBookings();
    }

    void showAllBookings() {
        tabs.setSelectedIndex(0);
        roleFilter.setSelectedIndex(0);
    }

    private void showBookings() {
        DefaultTableModel model = SwingUtils.readOnlyModel("Session", "Partner", "Your role", "Skill", "Scheduled for", "Status");
        for (Booking b : bookings) {
            boolean learner = b.getLearnerId() == currentUser.getId();
            if ((roleFilter.getSelectedIndex() == 1 && !learner) || (roleFilter.getSelectedIndex() == 2 && learner)) continue;
            model.addRow(new Object[]{b.getSessionTitle(), b.getPartnerName(currentUser.getId()), learner ? "Learner" : "Host",
                    b.getSkill(), b.getScheduledAt(), b.getStatus().toUpperCase(java.util.Locale.ROOT)});
        }
        bookingTable.setModel(model);
        int[] widths = {240, 145, 85, 120, 155, 110};
        for (int i = 0; i < widths.length; i++) bookingTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        updateActions();
    }

    private DefaultTableModel postModel(List<Session> sessions) {
        DefaultTableModel model = SwingUtils.readOnlyModel("Session", "Skill", "Scheduled for", "Status", "Bookings");
        for (Session s : sessions) {
            model.addRow(new Object[]{s.getTitle(), s.getSkill(), s.getScheduledAt(), s.getStatus().toUpperCase(java.util.Locale.ROOT), s.getBookingCount()});
        }
        return model;
    }

    private Session selectedPost() {
        int tab = tabs.getSelectedIndex();
        if (tab == 0) return null;
        JTable table = tab == 1 ? offeringTable : requestingTable;
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (tab == 1 ? offerings : requests).get(table.convertRowIndexToModel(row));
    }

    private void updateActions() {
        int tab = tabs.getSelectedIndex();
        roleFilter.setVisible(tab == 0);
        hostActions.setVisible(tab != 0);
        bookingActions.setVisible(tab == 0);
        
        if (tab == 0) {
            // Bookings tab - show appropriate action buttons
            int row = bookingTable.getSelectedRow();
            if (row < 0 || row >= bookingTable.getRowCount()) {
                // No selection
                messagePartner.setEnabled(false);
                acceptBooking.setEnabled(false);
                rejectBooking.setEnabled(false);
                completeBooking.setEnabled(false);
                cancelBooking.setEnabled(false);
            } else {
                // Get booking details from table
                String role = (String) bookingTable.getValueAt(row, 2); // "Host" or "Learner"
                String status = ((String) bookingTable.getValueAt(row, 5)).toLowerCase(); // "PENDING", "CONFIRMED", etc
                
                boolean isHost = "Host".equals(role);
                boolean isLearner = "Learner".equals(role);
                boolean isPending = "pending".equals(status);
                boolean isConfirmed = "confirmed".equals(status);
                
                messagePartner.setEnabled(true); // Always can message
                
                // Host actions
                acceptBooking.setVisible(isHost);
                rejectBooking.setVisible(isHost);
                acceptBooking.setEnabled(isHost && isPending);
                rejectBooking.setEnabled(isHost && isPending);
                
                // Complete button (both can mark complete if confirmed)
                completeBooking.setVisible(isConfirmed);
                completeBooking.setEnabled(isConfirmed);
                
                // Cancel button (learner can cancel pending/confirmed, host cannot cancel)
                cancelBooking.setVisible(isLearner);
                cancelBooking.setEnabled(isLearner && (isPending || isConfirmed));
            }
            
            int count = bookingTable.getRowCount();
            summary.setText(count == 0 ? "No bookings in this view." : count + (count == 1 ? " booking" : " bookings"));
        } else {
            // Session management tabs
            boolean selected = tab == 1 ? offeringTable.getSelectedRow() >= 0 : requestingTable.getSelectedRow() >= 0;
            manage.setEnabled(selected);
            edit.setEnabled(selected);
            delete.setEnabled(selected);
            boolean empty = (tab == 1 ? offerings : requests).isEmpty();
            summary.setText(empty ? "You haven't posted any " + (tab == 1 ? "offers" : "help requests") + " yet."
                    : "Select a session to manage it.");
        }
    }

    private void sessionChanged() {
        if (onChange == null) loadSessions();
        else onChange.run();
    }

    private void openEditor(Session session) {
        CreateSessionDialog dialog = new CreateSessionDialog(this, currentUser, session, this::sessionChanged);
        dialog.setVisible(true);
        Session saved = dialog.getSavedSession();
        if (saved != null) tabs.setSelectedIndex("offering".equals(saved.getSessionType()) ? 1 : 2);
    }

    private void deletePost() {
        Session s = selectedPost();
        if (s == null) return;
        
        // Check for existing bookings
        List<Booking> existingBookings = bookingDAO.getBookingsBySession(s.getId());
        if (!existingBookings.isEmpty()) {
            int activeBookings = 0;
            for (Booking b : existingBookings) {
                if (!"cancelled".equals(b.getStatus())) {
                    activeBookings++;
                }
            }
            
            if (activeBookings > 0) {
                String message = "Warning: This session has " + activeBookings + " active booking(s).\n\n" +
                                "Deleting will CANCEL all bookings and notify learners.\n\n" +
                                "Delete '" + s.getTitle() + "' anyway?";
                if (!SwingUtils.confirm(this, message)) {
                    return;
                }
                
                // Send cancellation messages to all learners
                for (Booking b : existingBookings) {
                    if (!"cancelled".equals(b.getStatus())) {
                        Message msg = new Message();
                        msg.setSenderId(currentUser.getId());
                        msg.setReceiverId(b.getLearnerId());
                        msg.setContent("Sorry, the session '" + s.getTitle() + "' has been cancelled by the host.");
                        messageDAO.sendMessage(msg);
                    }
                }
            }
        } else {
            if (!SwingUtils.confirm(this, "Delete '" + s.getTitle() + "'?")) {
                return;
            }
        }
        
        if (sessionDAO.deleteSession(s.getId())) {
            sessionChanged();
        } else {
            SwingUtils.showError(this, "Could not delete this session. Refresh and try again.");
        }
    }
    
    private void handleBookingAction(String newStatus) {
        int row = bookingTable.getSelectedRow();
        if (row < 0 || row >= bookingTable.getRowCount()) {
            SwingUtils.showError(this, "Please select a booking first.");
            return;
        }
        
        // Map filtered row to actual booking
        String sessionTitle = (String) bookingTable.getValueAt(row, 0);
        String partnerName = (String) bookingTable.getValueAt(row, 1);
        Booking foundBooking = null;
        for (Booking b : bookings) {
            if (b.getSessionTitle().equals(sessionTitle) && b.getPartnerName(currentUser.getId()).equals(partnerName)) {
                foundBooking = b;
                break;
            }
        }
        
        if (foundBooking == null) {
            SwingUtils.showError(this, "Could not find booking. Please refresh and try again.");
            return;
        }
        
        final Booking booking = foundBooking; // Make final for lambda
        String role = (String) bookingTable.getValueAt(row, 2);
        boolean isHost = "Host".equals(role);
        
        // Confirm action
        String actionName = newStatus.equals("confirmed") ? "accept" : 
                           newStatus.equals("completed") ? "mark as completed" : "cancel/reject";
        
        if (!SwingUtils.confirm(this, "Are you sure you want to " + actionName + " this booking?")) {
            return;
        }
        
        // Update status
        if (bookingDAO.updateBookingStatus(booking.getId(), newStatus)) {
            // Send auto-message to partner
            int partnerId = booking.getPartnerId(currentUser.getId());
            String messageContent = generateBookingMessage(newStatus, isHost, booking.getSessionTitle());
            
            Message msg = new Message();
            msg.setSenderId(currentUser.getId());
            msg.setReceiverId(partnerId);
            msg.setContent(messageContent);
            messageDAO.sendMessage(msg);
            
            // Show success and refresh
            SwingUtils.showSuccess(this, "Booking " + newStatus + "! A message has been sent to " + partnerName + ".");
            sessionChanged();
            
            // If completed, prompt for rating
            if ("completed".equals(newStatus)) {
                SwingUtilities.invokeLater(() -> {
                    if (SwingUtils.confirm(this, "Would you like to rate this session now?")) {
                        new RateSessionDialog(this, booking, currentUser, this::sessionChanged).setVisible(true);
                    }
                });
            }
        } else {
            SwingUtils.showError(this, "Could not update booking status. The booking may have already been updated.");
            sessionChanged();
        }
    }
    
    private void messageBookingPartner() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Please select a booking first.");
            return;
        }
        
        String partnerName = (String) bookingTable.getValueAt(row, 1);
        String sessionTitle = (String) bookingTable.getValueAt(row, 0);
        String role = (String) bookingTable.getValueAt(row, 2);
        
        // Find the booking
        Booking booking = null;
        for (Booking b : bookings) {
            if (b.getSessionTitle().equals(sessionTitle) && b.getPartnerName(currentUser.getId()).equals(partnerName)) {
                booking = b;
                break;
            }
        }
        
        if (booking == null) {
            SwingUtils.showError(this, "Could not find booking details.");
            return;
        }
        
        int partnerId = booking.getPartnerId(currentUser.getId());
        User partner = userDAO.getById(partnerId);
        
        if (partner == null) {
            SwingUtils.showError(this, "Could not load partner information.");
            return;
        }
        
        // Open quick message dialog
        JDialog dialog = new JDialog(this, "Message " + partnerName, true);
        FormPage page = new FormPage("Send a message");
        
        JTextArea messageArea = SwingUtils.createStyledTextArea(5, 30);
        messageArea.setText("Hi " + partnerName + ",\n\nRegarding the " + sessionTitle + " session...");
        
        page.section("About this booking");
        page.detail("Session", sessionTitle);
        page.detail("Partner", partnerName);
        page.detail("Your role", role);
        page.detail("Status", booking.getStatus().toUpperCase());
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
            msg.setReceiverId(partnerId);
            msg.setContent(content);
            
            if (messageDAO.sendMessage(msg)) {
                SwingUtils.showSuccess(dialog, "Message sent to " + partnerName + "!");
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
    
    private String generateBookingMessage(String status, boolean isHost, String sessionTitle) {
        switch (status) {
            case "confirmed":
                return isHost ? 
                    "Great news! I've accepted your booking for '" + sessionTitle + "'! Let's discuss the details here." :
                    "Thank you! I've confirmed my attendance for '" + sessionTitle + "'. Looking forward to it!";
            case "cancelled":
                return isHost ?
                    "Sorry, I cannot accommodate your booking for '" + sessionTitle + "' at this time. Feel free to book other sessions or message me for more information." :
                    "I need to cancel my booking for '" + sessionTitle + "'. Sorry for any inconvenience.";
            case "completed":
                return "The session '" + sessionTitle + "' has been marked as completed. Hope it was helpful! Please consider leaving a rating.";
            default:
                return "Booking status updated for '" + sessionTitle + "'.";
        }
    }
}
