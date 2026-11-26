package com.skillswap.view;

import com.skillswap.dao.UserDAO;
import com.skillswap.model.User;
import com.skillswap.util.SwingUtils;
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
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            SwingUtils.showError(this, "Name cannot be empty.");
            return;
        }
        currentUser.setName(name);
        currentUser.setStudentId(studentIdField.getText().trim());
        currentUser.setDepartment(departmentField.getText().trim());
        currentUser.setSemester((String) semesterBox.getSelectedItem());
        currentUser.setLearningMode((String) learningModeBox.getSelectedItem());
        currentUser.setPreferredTime(preferredTimeField.getText().trim());
        currentUser.setSkills(skillsArea.getText().trim());
        currentUser.setSkillsToLearn(skillsToLearnArea.getText().trim());
        currentUser.setBio(bioArea.getText().trim());

        if (userDAO.updateProfile(currentUser)) {
            SwingUtils.showSuccess(this, "Profile updated!");
            if (onSave != null) onSave.accept(currentUser);
            dispose();
        } else {
            SwingUtils.showError(this, "Failed to update profile.");
        }
    }
}
