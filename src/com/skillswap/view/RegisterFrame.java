package com.skillswap.view;

import com.skillswap.dao.UserDAO;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField nameField, emailField, studentIdField, departmentField, preferredTimeField;
    private JPasswordField passwordField;
    private JComboBox<String> semesterBox, learningModeBox;
    private JTextArea skillsArea, skillsToLearnArea, bioArea;
    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame() {
        setTitle("SkillSwap - Student Registration");
        setSize(640, 730);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtils.centerFrame(this);

        FormPage page = new FormPage("Create an account");

        nameField = SwingUtils.createStyledTextField(18);
        emailField = SwingUtils.createStyledTextField(18);
        passwordField = SwingUtils.createStyledPasswordField(18);
        studentIdField = SwingUtils.createStyledTextField(18);
        departmentField = SwingUtils.createStyledTextField(18);
        preferredTimeField = SwingUtils.createStyledTextField(18);

        semesterBox = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        learningModeBox = new JComboBox<>(new String[]{"Online", "In-Person", "Hybrid"});

        skillsArea = SwingUtils.createStyledTextArea(2, 18);
        skillsToLearnArea = SwingUtils.createStyledTextArea(2, 18);
        bioArea = SwingUtils.createStyledTextArea(3, 18);

        page.section("Account");
        page.field("Full name *", nameField);
        page.field("Email address *", emailField);
        page.field("Password *", passwordField);
        page.section("Student details");
        page.field("Student ID", studentIdField);
        page.field("Department", departmentField);
        page.field("Semester", semesterBox);
        page.section("Learning preferences");
        page.field("Learning mode", learningModeBox);
        page.field("Preferred time", preferredTimeField);
        page.field("Short bio", new JScrollPane(bioArea));
        page.section("Skills (separate with commas)");
        page.field("I can teach", new JScrollPane(skillsArea));
        page.field("I want to learn", new JScrollPane(skillsToLearnArea));

        JButton backBtn = SwingUtils.createStyledButton("Back to Login", SwingUtils.CARD);
        JButton submitBtn = SwingUtils.createStyledButton("Register", SwingUtils.PRIMARY_DARK);

        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        submitBtn.addActionListener(e -> handleRegister());

        page.actions(backBtn, submitBtn);
        setContentPane(page);
        getRootPane().setDefaultButton(submitBtn);
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            SwingUtils.showError(this, "Name, Email, and Password are required.");
            return;
        }
        if (!email.contains("@")) {
            SwingUtils.showError(this, "Please enter a valid email address.");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(pass);
        user.setStudentId(studentIdField.getText().trim());
        user.setDepartment(departmentField.getText().trim());
        user.setSemester((String) semesterBox.getSelectedItem());
        user.setLearningMode((String) learningModeBox.getSelectedItem());
        user.setPreferredTime(preferredTimeField.getText().trim());
        user.setSkills(skillsArea.getText().trim());
        user.setSkillsToLearn(skillsToLearnArea.getText().trim());
        user.setBio(bioArea.getText().trim());

        if (userDAO.register(user)) {
            SwingUtils.showSuccess(this, "Registration successful! Please log in.");
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            SwingUtils.showError(this, "Registration failed. Email may already be registered.");
        }
    }
}
