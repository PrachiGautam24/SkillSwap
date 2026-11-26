package com.skillswap.view;

import com.skillswap.dao.BookingDAO;
import com.skillswap.dao.RatingDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MyExchangesFrame extends JFrame {
    private final User currentUser;
    private JTable exchangeTable;
    private final JButton rateBtn = SwingUtils.createStyledButton("Rate session", SwingUtils.PRIMARY_DARK);
    private List<Booking> completedBookings;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RatingDAO ratingDAO = new RatingDAO();

    public MyExchangesFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("SkillSwap - Completed Exchanges & Ratings");
        setSize(860, 480);
        SwingUtils.centerFrame(this);
        setContentPane(SwingUtils.page());
        add(SwingUtils.heading("Completed exchanges", null), BorderLayout.NORTH);
        exchangeTable = SwingUtils.createTable("Completed sessions will appear here.");
        add(new JScrollPane(exchangeTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        rateBtn.addActionListener(e -> openRateDialog());
        bottom.add(rateBtn);
        add(bottom, BorderLayout.SOUTH);

        exchangeTable.getSelectionModel().addListSelectionListener(e -> updateRateAction());
        loadExchanges();
    }

    void loadExchanges() {
        completedBookings = bookingDAO.getBookingsByUser(currentUser.getId()).stream()
                .filter(b -> "completed".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());

        DefaultTableModel model = SwingUtils.readOnlyModel("Session", "Partner", "Skill", "Scheduled for", "Status", "Rating");

        for (Booking b : completedBookings) {
            String partner = b.getPartnerName(currentUser.getId());
            String ratingStr = b.getRatingValue() > 0 ? b.getRatingValue() + " / 5" : "Unrated";
            model.addRow(new Object[]{b.getSessionTitle(), partner, b.getSkill(), b.getScheduledAt(), b.getStatus().toUpperCase(), ratingStr});
        }
        exchangeTable.setModel(model);
        exchangeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        updateRateAction();
    }

    private void updateRateAction() {
        int row = exchangeTable.getSelectedRow();
        if (row < 0) { 
            rateBtn.setEnabled(false); 
            return; 
        }
        Booking b = completedBookings.get(exchangeTable.convertRowIndexToModel(row));
        
        // Check if current user has already rated
        boolean isLearner = b.getLearnerId() == currentUser.getId();
        boolean hasRated = isLearner ? b.isLearnerHasRated() : b.isTutorHasRated();
        
        rateBtn.setEnabled(!hasRated);
        rateBtn.setText(hasRated ? "Already Rated" : "Rate Session");
    }

    private void openRateDialog() {
        int row = exchangeTable.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Please select a completed session to rate.");
            return;
        }
        Booking selected = completedBookings.get(exchangeTable.convertRowIndexToModel(row));
        
        // Check if already rated
        boolean isLearner = selected.getLearnerId() == currentUser.getId();
        boolean hasRated = isLearner ? selected.isLearnerHasRated() : selected.isTutorHasRated();
        
        if (hasRated) {
            SwingUtils.showError(this, "You have already rated this session.");
            return;
        }
        
        new RateSessionDialog(this, selected, currentUser, this::loadExchanges).setVisible(true);
    }
}
