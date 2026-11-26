package com.skillswap.view;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class SendRequestDialog extends JDialog {
    private JTextField offeringField, requestingField;
    private JTextArea messageArea;
    private final User currentUser;
    private final User targetUser;
    private final ExchangeRequestDAO requestDAO = new ExchangeRequestDAO();

    public SendRequestDialog(Frame owner, User currentUser, User targetUser) {
        super(owner, "Send Skill Exchange Request", true);
        this.currentUser = currentUser;
        this.targetUser = targetUser;

        setSize(600, 420);
        SwingUtils.centerFrame(this);

        FormPage page = new FormPage("Send an exchange request");

        offeringField = SwingUtils.createStyledTextField(20);
        offeringField.setText(currentUser.getSkills());
        requestingField = SwingUtils.createStyledTextField(20);
        requestingField.setText(targetUser.getSkills());
        messageArea = SwingUtils.createStyledTextArea(4, 20);
        messageArea.setText("Hi " + targetUser.getName() + ", would love to swap skills with you!");

        page.section("To " + targetUser.getName());
        page.field("Skill you offer *", offeringField);
        page.field("Skill you request *", requestingField);
        page.field("Message", new JScrollPane(messageArea));

        JButton cancelBtn = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton sendBtn = SwingUtils.createStyledButton("Send Request", SwingUtils.PRIMARY_DARK);

        cancelBtn.addActionListener(e -> dispose());
        sendBtn.addActionListener(e -> sendRequest());
        page.actions(cancelBtn, sendBtn);
        setContentPane(page);
        getRootPane().setDefaultButton(sendBtn);
    }

    private void sendRequest() {
        String off = offeringField.getText().trim();
        String req = requestingField.getText().trim();
        if (off.isEmpty() || req.isEmpty()) {
            SwingUtils.showError(this, "Both offering and requesting skills are required.");
            return;
        }

        ExchangeRequest er = new ExchangeRequest();
        er.setFromUserId(currentUser.getId());
        er.setToUserId(targetUser.getId());
        er.setSkillOffering(off);
        er.setSkillRequesting(req);
        er.setMessage(messageArea.getText().trim());

        if (requestDAO.createRequest(er)) {
            SwingUtils.showSuccess(this, "Exchange request sent to " + targetUser.getName() + "!");
            dispose();
        } else {
            SwingUtils.showError(this, "Failed to send exchange request.");
        }
    }
}
