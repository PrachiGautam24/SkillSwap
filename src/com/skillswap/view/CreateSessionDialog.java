package com.skillswap.view;

import com.skillswap.dao.SessionDAO;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class CreateSessionDialog extends JDialog {
    private JTextField titleField, skillField, scheduledAtField;
    private JTextArea descriptionArea;
    private JRadioButton offeringRadio, requestingRadio;
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
        scheduledAtField = SwingUtils.createStyledTextField(20);
        scheduledAtField.setText(java.time.LocalDate.now().plusDays(1) + " 17:00");
        descriptionArea = SwingUtils.createStyledTextArea(4, 20);

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
            scheduledAtField.setText(editingSession.getScheduledAt());
            descriptionArea.setText(editingSession.getDescription());
            if ("requesting".equalsIgnoreCase(editingSession.getSessionType())) {
                requestingRadio.setSelected(true);
            }
        }

        page.field("Session type", radioPanel);
        page.field("Session title *", titleField);
        page.field("Skill *", skillField);
        page.field("Scheduled for", scheduledAtField);
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

        Session s = new Session();
        if (editingSession != null) s.setId(editingSession.getId());
        s.setTutorId(currentUser.getId());
        s.setTitle(title);
        s.setSkill(skill);
        s.setScheduledAt(scheduledAtField.getText().trim());
        s.setDescription(descriptionArea.getText().trim());
        s.setSessionType(offeringRadio.isSelected() ? "offering" : "requesting");
        s.setStatus("open");

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
