package com.skillswap.view;

import com.skillswap.dao.UserDAO;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
import com.skillswap.util.ValidationUtil;
import com.skillswap.util.ValidationUtil.ValidationResult;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class EditProfileDialog extends JDialog {
    private final User currentUser;
    private final Consumer<User> onSave;
    private JTextField nameField, studentIdField, departmentField, preferredTimeField;
    private JComboBox<String> semesterBox, learningModeBox;
    private JTextArea skillsArea, skillsToLearnArea, bioArea;
    private final UserDAO userDAO = new UserDAO();

    public EditProfileDialog(Frame owner, User currentUser, Consumer<User> onSave) {
        super(owner, "Edit Profile", true);
        this.currentUser = currentUser;
        this.onSave = onSave;

        setSize(620, 680);
        SwingUtils.centerFrame(this);

        FormPage page = new FormPage("Edit profile");

        nameField = SwingUtils.createStyledTextField(18);
        nameField.setText(currentUser.getName());
        studentIdField = SwingUtils.createStyledTextField(18);
        studentIdField.setText(currentUser.getStudentId());
        departmentField = SwingUtils.createStyledTextField(18);
        departmentField.setText(currentUser.getDepartment());
        preferredTimeField = SwingUtils.createStyledTextField(18);
        preferredTimeField.setText(currentUser.getPreferredTime());

        semesterBox = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        semesterBox.setSelectedItem(currentUser.getSemester());

        learningModeBox = new JComboBox<>(new String[]{"Online", "In-Person", "Hybrid"});
        learningModeBox.setSelectedItem(currentUser.getLearningMode());

        skillsArea = SwingUtils.createStyledTextArea(2, 18);
        skillsArea.setText(currentUser.getSkills());
        skillsToLearnArea = SwingUtils.createStyledTextArea(2, 18);
        skillsToLearnArea.setText(currentUser.getSkillsToLearn());
        bioArea = SwingUtils.createStyledTextArea(3, 18);
        bioArea.setText(currentUser.getBio());

        page.section("Student details");
        page.field("Full name *", nameField);
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

        JButton cancelBtn = SwingUtils.createStyledButton("Cancel", SwingUtils.CARD);
        JButton saveBtn = SwingUtils.createStyledButton("Save Changes", SwingUtils.PRIMARY_DARK);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> saveChanges());
        page.actions(cancelBtn, saveBtn);
        setContentPane(page);
        getRootPane().setDefaultButton(saveBtn);
    }

    private void saveChanges() {
        // Get all values
        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String department = departmentField.getText().trim();
        String skills = skillsArea.getText().trim();
        String skillsToLearn = skillsToLearnArea.getText().trim();
        String bio = bioArea.getText().trim();
        
        // Validate all fields
        ValidationResult nameValid = ValidationUtil.validateName(name);
        if (!nameValid.isValid()) {
            SwingUtils.showError(this, nameValid.getMessage());
            nameField.requestFocus();
            return;
        }
        
        ValidationResult studentIdValid = ValidationUtil.validateStudentId(studentId);
        if (!studentIdValid.isValid()) {
            SwingUtils.showError(this, studentIdValid.getMessage());
            studentIdField.requestFocus();
            return;
        }
        
        ValidationResult deptValid = ValidationUtil.validateDepartment(department);
        if (!deptValid.isValid()) {
            SwingUtils.showError(this, deptValid.getMessage());
            departmentField.requestFocus();
            return;
        }
        
        ValidationResult skillsValid = ValidationUtil.validateSkills(skills, "Skills you can teach");
        if (!skillsValid.isValid()) {
            SwingUtils.showError(this, skillsValid.getMessage());
            skillsArea.requestFocus();
            return;
        }
        
        ValidationResult skillsToLearnValid = ValidationUtil.validateSkills(skillsToLearn, "Skills you want to learn");
        if (!skillsToLearnValid.isValid()) {
            SwingUtils.showError(this, skillsToLearnValid.getMessage());
            skillsToLearnArea.requestFocus();
            return;
        }
        
        ValidationResult bioValid = ValidationUtil.validateBio(bio);
        if (!bioValid.isValid()) {
            SwingUtils.showError(this, bioValid.getMessage());
            bioArea.requestFocus();
            return;
        }
        
        // All validation passed - update user
        currentUser.setName(name);
        currentUser.setStudentId(studentId.toUpperCase());
        currentUser.setDepartment(department);
        currentUser.setSemester((String) semesterBox.getSelectedItem());
        currentUser.setLearningMode((String) learningModeBox.getSelectedItem());
        currentUser.setPreferredTime(preferredTimeField.getText().trim());
        currentUser.setSkills(skills);
        currentUser.setSkillsToLearn(skillsToLearn);
        currentUser.setBio(bio);

        if (userDAO.updateProfile(currentUser)) {
            SwingUtils.showSuccess(this, "Profile updated successfully!");
            if (onSave != null) onSave.accept(currentUser);
            dispose();
        } else {
            SwingUtils.showError(this, "Failed to update profile. Please try again.");
        }
    }
}
