package com.skillswap.view;

import com.skillswap.dao.UserDAO;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import com.skillswap.util.ValidationUtil;
import com.skillswap.util.ValidationUtil.ValidationResult;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField nameField, emailField, studentIdField, departmentField, preferredTimeField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> semesterBox, learningModeBox;
    private JTextArea skillsArea, skillsToLearnArea, bioArea;
    private JLabel passwordStrengthLabel;
    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame() {
        setTitle("SkillSwap - Student Registration");
        setSize(640, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtils.centerFrame(this);

        FormPage page = new FormPage("Create an account");

        nameField = SwingUtils.createStyledTextField(18);
        emailField = SwingUtils.createStyledTextField(18);
        passwordField = SwingUtils.createStyledPasswordField(18);
        confirmPasswordField = SwingUtils.createStyledPasswordField(18);
        studentIdField = SwingUtils.createStyledTextField(18);
        departmentField = SwingUtils.createStyledTextField(18);
        preferredTimeField = SwingUtils.createStyledTextField(18);

        semesterBox = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        learningModeBox = new JComboBox<>(new String[]{"Online", "In-Person", "Hybrid"});

        skillsArea = SwingUtils.createStyledTextArea(2, 18);
        skillsToLearnArea = SwingUtils.createStyledTextArea(2, 18);
        bioArea = SwingUtils.createStyledTextArea(3, 18);

        // Password strength indicator
        passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setFont(SwingUtils.BODY_FONT);
        
        // Add real-time password validation
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
        });

        page.section("Account");
        page.field("Full name *", nameField);
        page.field("Email address *", emailField);
        page.field("Password *", passwordField);
        
        JPanel passPanel = new JPanel(new BorderLayout(0, 4));
        passPanel.setOpaque(false);
        passPanel.add(confirmPasswordField, BorderLayout.NORTH);
        passPanel.add(passwordStrengthLabel, BorderLayout.SOUTH);
        page.field("Confirm password *", passPanel);
        
        JLabel passHint = new JLabel("<html><i>Password must be 8+ characters with uppercase, lowercase, and digit</i></html>");
        passHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passHint.setForeground(SwingUtils.TEXT_SECONDARY);
        page.field("", passHint);
        
        page.section("Student details");
        page.field("Student ID", studentIdField);
        page.field("Department", departmentField);
        page.field("Semester", semesterBox);
        page.section("Learning preferences");
        page.field("Learning mode", learningModeBox);
        page.field("Preferred time", preferredTimeField);
        page.field("Short bio (max 500 chars)", new JScrollPane(bioArea));
        page.section("Skills (separate with commas) *");
        page.field("I can teach *", new JScrollPane(skillsArea));
        page.field("I want to learn *", new JScrollPane(skillsToLearnArea));

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

    private void updatePasswordStrength() {
        String pass = new String(passwordField.getPassword());
        if (pass.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }
        
        ValidationResult result = ValidationUtil.validatePassword(pass);
        if (result.isValid()) {
            passwordStrengthLabel.setText("✓ " + result.getMessage());
            passwordStrengthLabel.setForeground(SwingUtils.SUCCESS);
        } else {
            passwordStrengthLabel.setText("✗ " + result.getMessage());
            passwordStrengthLabel.setForeground(SwingUtils.DANGER);
        }
    }

    private void handleRegister() {
        // Get all field values
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String studentId = studentIdField.getText().trim();
        String department = departmentField.getText().trim();
        String skills = skillsArea.getText().trim();
        String skillsToLearn = skillsToLearnArea.getText().trim();
        String bio = bioArea.getText().trim();

        // Validate all required fields
        ValidationResult nameValidation = ValidationUtil.validateName(name);
        if (!nameValidation.isValid()) {
            SwingUtils.showError(this, nameValidation.getMessage());
            nameField.requestFocus();
            return;
        }

        ValidationResult emailValidation = ValidationUtil.validateEmail(email);
        if (!emailValidation.isValid()) {
            SwingUtils.showError(this, emailValidation.getMessage());
            emailField.requestFocus();
            return;
        }

        ValidationResult passwordValidation = ValidationUtil.validatePassword(pass);
        if (!passwordValidation.isValid()) {
            SwingUtils.showError(this, passwordValidation.getMessage());
            passwordField.requestFocus();
            return;
        }

        ValidationResult passwordMatchValidation = ValidationUtil.validatePasswordMatch(pass, confirmPass);
        if (!passwordMatchValidation.isValid()) {
            SwingUtils.showError(this, passwordMatchValidation.getMessage());
            confirmPasswordField.requestFocus();
            return;
        }

        // Validate optional but formatted fields
        ValidationResult studentIdValidation = ValidationUtil.validateStudentId(studentId);
        if (!studentIdValidation.isValid()) {
            SwingUtils.showError(this, studentIdValidation.getMessage());
            studentIdField.requestFocus();
            return;
        }

        ValidationResult departmentValidation = ValidationUtil.validateDepartment(department);
        if (!departmentValidation.isValid()) {
            SwingUtils.showError(this, departmentValidation.getMessage());
            departmentField.requestFocus();
            return;
        }

        // Validate skills (required fields)
        ValidationResult skillsValidation = ValidationUtil.validateSkills(skills, "Skills you can teach");
        if (!skillsValidation.isValid()) {
            SwingUtils.showError(this, skillsValidation.getMessage());
            skillsArea.requestFocus();
            return;
        }

        ValidationResult skillsToLearnValidation = ValidationUtil.validateSkills(skillsToLearn, "Skills you want to learn");
        if (!skillsToLearnValidation.isValid()) {
            SwingUtils.showError(this, skillsToLearnValidation.getMessage());
            skillsToLearnArea.requestFocus();
            return;
        }

        ValidationResult bioValidation = ValidationUtil.validateBio(bio);
        if (!bioValidation.isValid()) {
            SwingUtils.showError(this, bioValidation.getMessage());
            bioArea.requestFocus();
            return;
        }

        // All validations passed - create user
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase()); // Store email in lowercase
        user.setPassword(pass);
        user.setStudentId(studentId.toUpperCase());
        user.setDepartment(department);
        user.setSemester((String) semesterBox.getSelectedItem());
        user.setLearningMode((String) learningModeBox.getSelectedItem());
        user.setPreferredTime(preferredTimeField.getText().trim());
        user.setSkills(skills);
        user.setSkillsToLearn(skillsToLearn);
        user.setBio(bio);

        if (userDAO.register(user)) {
            SwingUtils.showSuccess(this, "Registration successful! You can now log in with your credentials.");
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            SwingUtils.showError(this, "Registration failed. This email address is already registered.");
            emailField.requestFocus();
        }
    }
}
