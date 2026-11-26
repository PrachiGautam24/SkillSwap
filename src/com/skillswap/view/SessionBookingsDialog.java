package com.skillswap.view;

import com.skillswap.dao.BookingDAO;
import com.skillswap.model.Booking;
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
            loadBookings();
            if (onUpdate != null) onUpdate.run();
        } else {
            loadBookings();
            SwingUtils.showError(this, "Could not update this booking. Check its current status and try again.");
        }
    }
}
