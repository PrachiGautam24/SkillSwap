package com.skillswap.view;

import com.skillswap.dao.SessionDAO;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class CreateSessionDialog extends JDialog {
    private JTextField titleField, skillField;
    private JTextArea descriptionArea;
    private JRadioButton offeringRadio, requestingRadio;
    private JSpinner dateSpinner, timeSpinner, capacitySpinner;
    private final User currentUser;
    private final Session editingSession;
    private Session savedSession;
    private final Runnable onSuccess;
    private final SessionDAO sessionDAO = new SessionDAO();

    public CreateSessionDialog(Frame owner, User currentUser, Runnable onSuccess) {
        this(owner, currentUser, null, onSuccess);
    }

    public CreateSessionDialog(Frame owner, User currentUser, Session editingSession, Runnable onSuccess) {
        super(owner, editingSession == null ? "Create New Session" : "Edit Session", true);
        this.currentUser = currentUser;
        this.editingSession = editingSession;
        this.onSuccess = onSuccess;

        setSize(600, 470);
        SwingUtils.centerFrame(this);

        FormPage page = new FormPage(editingSession == null ? "Create a session" : "Edit session");

        titleField = SwingUtils.createStyledTextField(20);
        skillField = SwingUtils.createStyledTextField(20);
        descriptionArea = SwingUtils.createStyledTextArea(4, 20);

        // Date spinner (starting tomorrow)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Date tomorrow = cal.getTime();
        java.util.Date oneYearFromNow = new java.util.Date(tomorrow.getTime() + 365L * 24 * 60 * 60 * 1000);
        
        dateSpinner = new JSpinner(new SpinnerDateModel(tomorrow, tomorrow, oneYearFromNow, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        // Time spinner
        cal.setTime(tomorrow);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 15);
        cal.set(java.util.Calendar.MINUTE, 0);
        timeSpinner = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.HOUR_OF_DAY));
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        
        // Capacity spinner
        capacitySpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));

        offeringRadio = new JRadioButton("Offer help", true);
        requestingRadio = new JRadioButton("Request help");
        offeringRadio.setOpaque(false);
        offeringRadio.setForeground(SwingUtils.TEXT_PRIMARY);
        requestingRadio.setOpaque(false);
        requestingRadio.setForeground(SwingUtils.TEXT_PRIMARY);

        ButtonGroup group = new ButtonGroup();
        group.add(offeringRadio);
        group.add(requestingRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(offeringRadio);
        radioPanel.add(requestingRadio);

        if (editingSession != null) {
            titleField.setText(editingSession.getTitle());
            skillField.setText(editingSession.getSkill());
            descriptionArea.setText(editingSession.getDescription());
            
            // Parse existing scheduled time
            try {
                String[] parts = editingSession.getScheduledAt().split(" ");
                if (parts.length == 2) {
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
                    dateSpinner.setValue(dateFormat.parse(parts[0]));
                    timeSpinner.setValue(timeFormat.parse(parts[1]));
                }
            } catch (Exception e) {
                // Use default values if parsing fails
            }
            
            if (editingSession.getMaxCapacity() > 0) {
                capacitySpinner.setValue(editingSession.getMaxCapacity());
            }
            
            if ("requesting".equalsIgnoreCase(editingSession.getSessionType())) {
                requestingRadio.setSelected(true);
            }
        }

        page.field("Session type", radioPanel);
        page.field("Session title *", titleField);
        page.field("Skill *", skillField);
        page.field("Date *", dateSpinner);
        page.field("Time *", timeSpinner);
        page.field("Max participants", capacitySpinner);
        page.field("Description", new JScrollPane(descriptionArea));

        JButton cancelBtn = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton saveBtn = SwingUtils.createStyledButton(editingSession == null ? "Create Session" : "Update Session", SwingUtils.PRIMARY_DARK);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> saveSession());
        page.actions(cancelBtn, saveBtn);
        setContentPane(page);
        getRootPane().setDefaultButton(saveBtn);
    }

    private void saveSession() {
        String title = titleField.getText().trim();
        String skill = skillField.getText().trim();
        if (title.isEmpty() || skill.isEmpty()) {
            SwingUtils.showError(this, "Title and Skill are required.");
            return;
        }

        // Format date and time
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
        String scheduledAt = dateFormat.format(dateSpinner.getValue()) + " " + timeFormat.format(timeSpinner.getValue());

        Session s = new Session();
        if (editingSession != null) s.setId(editingSession.getId());
        s.setTutorId(currentUser.getId());
        s.setTitle(title);
        s.setSkill(skill);
        s.setScheduledAt(scheduledAt);
        s.setDescription(descriptionArea.getText().trim());
        s.setSessionType(offeringRadio.isSelected() ? "offering" : "requesting");
        s.setStatus("open");
        s.setMaxCapacity((Integer) capacitySpinner.getValue());

        boolean ok = (editingSession == null) ? sessionDAO.createSession(s) : sessionDAO.updateSession(s);
        if (ok) {
            savedSession = s;
            if (onSuccess != null) onSuccess.run();
            SwingUtils.showSuccess(this, "Session saved successfully!");
            dispose();
        } else {
            SwingUtils.showError(this, "Failed to save session.");
        }
    }

    public Session getSavedSession() { return savedSession; }
}
