package com.skillswap.view;

import com.skillswap.dao.RatingDAO;
import com.skillswap.model.Booking;
import com.skillswap.model.Rating;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class RateSessionDialog extends JDialog {
    private final Booking booking;
    private final User currentUser;
    private final Runnable onSuccess;
    private final JLabel score = SwingUtils.createLabel("", SwingUtils.SUBTITLE_FONT, SwingUtils.AMBER);
    private JSlider ratingSlider;
    private JTextArea reviewArea;
    private final RatingDAO ratingDAO = new RatingDAO();
    private final boolean isLearner;

    public RateSessionDialog(Frame owner, Booking booking, User currentUser, Runnable onSuccess) {
        super(owner, "Rate Session: " + booking.getSessionTitle(), true);
        this.booking = booking;
        this.currentUser = currentUser;
        this.onSuccess = onSuccess;
        this.isLearner = booking.getLearnerId() == currentUser.getId();

        setSize(550, 440);
        SwingUtils.centerFrame(this);
        FormPage page = new FormPage(isLearner ? "Rate your tutor" : "Rate your learner");
        
        String partnerRole = isLearner ? "Tutor: " : "Learner: ";
        String partnerName = isLearner ? booking.getTutorName() : booking.getLearnerName();
        
        page.section(booking.getSessionTitle());
        page.detail(partnerRole, partnerName);
        
        ratingSlider = new JSlider(1, 5, 5);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setOpaque(false);
        ratingSlider.getAccessibleContext().setAccessibleName("Session rating from 1 to 5");
        ratingSlider.addChangeListener(e -> showScore());
        showScore();

        JPanel rating = new JPanel(new BorderLayout(0, 5));
        rating.setOpaque(false);
        rating.add(score, BorderLayout.NORTH);
        rating.add(ratingSlider, BorderLayout.CENTER);
        page.field("Your rating", rating);
        reviewArea = SwingUtils.createStyledTextArea(4, 18);
        page.field("Written review", new JScrollPane(reviewArea));
        JButton cancelBtn = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton submitBtn = SwingUtils.createStyledButton("Submit Rating", SwingUtils.PRIMARY_DARK);
        cancelBtn.addActionListener(e -> dispose());
        submitBtn.addActionListener(e -> submitRating());
        page.actions(cancelBtn, submitBtn);
        setContentPane(page);
        getRootPane().setDefaultButton(submitBtn);
    }

    private void showScore() {
        score.setText(ratingSlider.getValue() + " out of 5");
    }

    private void submitRating() {
        Rating r = new Rating();
        r.setBookingId(booking.getId());
        r.setRating(ratingSlider.getValue());
        r.setReview(reviewArea.getText().trim());
        r.setRatedByLearner(isLearner);

        if (ratingDAO.createRating(r)) {
            SwingUtils.showSuccess(this, "Thank you for your feedback!");
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            SwingUtils.showError(this, "Could not submit rating. You may have already rated this session.");
        }
    }
}
