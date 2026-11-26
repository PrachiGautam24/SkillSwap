package com.skillswap.view;

import com.skillswap.dao.UserDAO;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("SkillSwap - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel page = SwingUtils.page();
        JLabel brand = SwingUtils.wordmark();
        brand.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        page.add(brand, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SwingUtils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SwingUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(SwingUtils.createLabel("Sign in", SwingUtils.TITLE_FONT, SwingUtils.TEXT_PRIMARY), c);

        emailField = SwingUtils.createStyledTextField(24);
        emailField.setText("alice@uni.edu");
        passwordField = SwingUtils.createStyledPasswordField(24);
        passwordField.setText("pass123");
        for (JTextField field : new JTextField[]{emailField, passwordField}) {
            JLabel label = SwingUtils.createLabel(field == emailField ? "Email address" : "Password",
                    SwingUtils.BOLD_FONT, SwingUtils.TEXT_PRIMARY);
            label.setLabelFor(field);
            c.insets = new Insets(0, 0, 5, 0);
            card.add(label, c);
            c.insets = new Insets(0, 0, 16, 0);
            card.add(field, c);
        }
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        JButton loginBtn = SwingUtils.createStyledButton("Login", SwingUtils.PRIMARY_DARK);
        JButton registerBtn = SwingUtils.createStyledButton("Register", SwingUtils.CARD);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        btnPanel.add(registerBtn);
        btnPanel.add(loginBtn);
        c.insets = new Insets(2, 0, 0, 0);
        card.add(btnPanel, c);
        page.add(card, BorderLayout.CENTER);
        setContentPane(page);
        pack();
        setMinimumSize(getSize());
        SwingUtils.centerFrame(this);
        getRootPane().setDefaultButton(loginBtn);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            SwingUtils.showError(this, "Please enter both email and password.");
            return;
        }

        User user = userDAO.login(email, password);
        if (user != null) {
            dispose();
            new DashboardFrame(user).setVisible(true);
        } else {
            SwingUtils.showError(this, "Invalid email or password.");
        }
    }
}
