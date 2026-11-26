package com.skillswap.view;

import com.skillswap.dao.BookingDAO;
import com.skillswap.dao.MessageDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.Message;
import com.skillswap.model.Session;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SessionBookingsDialog extends JDialog {
    private final Session session;
    private final Runnable onUpdate;
    private JTable bookingTable;
    private final JButton confirmBtn = SwingUtils.createStyledButton("Confirm", SwingUtils.PRIMARY_DARK);
    private final JButton completeBtn = SwingUtils.createStyledButton("Complete", SwingUtils.SUCCESS);
    private final JButton cancelBtn = SwingUtils.createStyledButton("Cancel booking", SwingUtils.DANGER);
    private List<Booking> bookingList;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    public SessionBookingsDialog(Frame owner, Session session, Runnable onUpdate) {
        super(owner, "Bookings for: " + session.getTitle(), true);
        this.session = session;
        this.onUpdate = onUpdate;

        setSize(700, 420);
        SwingUtils.centerFrame(this);
        setContentPane(SwingUtils.page());
        add(SwingUtils.heading(session.getTitle(), null), BorderLayout.NORTH);
        bookingTable = SwingUtils.createTable("No learners have booked this session yet.");
        add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);


        confirmBtn.addActionListener(e -> changeStatus("confirmed"));
        completeBtn.addActionListener(e -> changeStatus("completed"));
        cancelBtn.addActionListener(e -> changeStatus("cancelled"));

        btns.add(confirmBtn);
        btns.add(completeBtn);
        btns.add(cancelBtn);
        add(btns, BorderLayout.SOUTH);

        bookingTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        loadBookings();
    }

    private void loadBookings() {
        bookingList = bookingDAO.getBookingsBySession(session.getId());
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Learner Name", "Email", "Status", "Booked Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Booking b : bookingList) {
            model.addRow(new Object[]{b.getLearnerName(), b.getLearnerEmail(), b.getStatus().toUpperCase(), b.getCreatedAt()});
        }
        bookingTable.setModel(model);
        updateActions();
    }

    private void updateActions() {
        int row = bookingTable.getSelectedRow();
        String status = row < 0 ? "" : bookingList.get(bookingTable.convertRowIndexToModel(row)).getStatus();
        confirmBtn.setEnabled("pending".equals(status));
        completeBtn.setEnabled("confirmed".equals(status));
        cancelBtn.setEnabled("pending".equals(status) || "confirmed".equals(status));
    }

    private void changeStatus(String status) {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Please select a booking first.");
            return;
        }
        Booking b = bookingList.get(bookingTable.convertRowIndexToModel(row));
        if (bookingDAO.updateBookingStatus(b.getId(), status)) {
            // Send auto-message to learner
            String messageContent = generateBookingMessage(status, session.getTitle());
            Message msg = new Message();
            msg.setSenderId(session.getTutorId());
            msg.setReceiverId(b.getLearnerId());
            msg.setContent(messageContent);
            messageDAO.sendMessage(msg);
            
            SwingUtils.showSuccess(this, "Booking " + status + "! Message sent to " + b.getLearnerName() + ".");
            loadBookings();
            if (onUpdate != null) onUpdate.run();
        } else {
            loadBookings();
            SwingUtils.showError(this, "Could not update this booking. Check its current status and try again.");
        }
    }
    
    private String generateBookingMessage(String status, String title) {
        switch (status) {
            case "confirmed":
                return "Great news! Your booking for '" + title + "' has been confirmed! Let's discuss the details in Messages.";
            case "cancelled":
                return "Sorry, your booking for '" + title + "' was declined. Feel free to book other sessions or message me for more information.";
            case "completed":
                return "The session '" + title + "' has been marked as completed. Hope it was helpful! Please consider leaving a rating.";
            default:
                return "Booking status updated for '" + title + "'.";
        }
    }
}
