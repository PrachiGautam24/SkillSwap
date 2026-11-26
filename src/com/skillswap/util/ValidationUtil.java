package com.skillswap.util;

import java.util.regex.Pattern;

/**
 * Comprehensive validation utility for user input validation
 */
public class ValidationUtil {
    
    // Email validation pattern - standard RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Password requirements: at least 8 characters, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );
    
    // Student ID format: letters and numbers only
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Z0-9]{3,15}$");
    
    // Name validation: only letters, spaces, hyphens, apostrophes
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{2,100}$");
    
    /**
     * Validates email format
     * @param email Email to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }
        
        email = email.trim();
        
        if (email.length() > 100) {
            return new ValidationResult(false, "Email must be less than 100 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Invalid email format (e.g., user@university.edu)");
        }
        
        return new ValidationResult(true, "Valid email");
    }
    
    /**
     * Validates password strength
     * @param password Password to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters long");
        }
        
        if (password.length() > 100) {
            return new ValidationResult(false, "Password must be less than 100 characters");
        }
        
        if (!password.matches(".*[a-z].*")) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }
        
        // Optional: Check for special characters (recommended but not enforced)
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return new ValidationResult(true, "Strong password (consider adding special characters for extra security)");
        }
        
        return new ValidationResult(true, "Strong password");
    }
    
    /**
     * Validates password confirmation match
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @return ValidationResult with status and message
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return new ValidationResult(false, "Password confirmation is required");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }
        
        return new ValidationResult(true, "Passwords match");
    }
    
    /**
     * Validates user full name
     * @param name Name to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Full name is required");
        }
        
        name = name.trim();
        
        if (name.length() < 2) {
            return new ValidationResult(false, "Name must be at least 2 characters long");
        }
        
        if (name.length() > 100) {
            return new ValidationResult(false, "Name must be less than 100 characters");
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            return new ValidationResult(false, "Name can only contain letters, spaces, hyphens, and apostrophes");
        }
        
        return new ValidationResult(true, "Valid name");
    }
    
    /**
     * Validates student ID format
     * @param studentId Student ID to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new ValidationResult(true, "Student ID is optional"); // Optional field
        }
        
        studentId = studentId.trim().toUpperCase();
        
        if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
            return new ValidationResult(false, "Student ID must be 3-15 characters (letters and numbers only)");
        }
        
        return new ValidationResult(true, "Valid student ID");
    }
    
    /**
     * Validates department field
     * @param department Department to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return new ValidationResult(true, "Department is optional");
        }
        
        department = department.trim();
        
        if (department.length() < 2 || department.length() > 100) {
            return new ValidationResult(false, "Department must be 2-100 characters");
        }
        
        return new ValidationResult(true, "Valid department");
    }
    
    /**
     * Validates skills field
     * @param skills Skills to validate
     * @param fieldName Name of the field (for error messages)
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateSkills(String skills, String fieldName) {
        if (skills == null || skills.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " cannot be empty");
        }
        
        skills = skills.trim();
        
        if (skills.length() < 3) {
            return new ValidationResult(false, fieldName + " must be at least 3 characters");
        }
        
        if (skills.length() > 255) {
            return new ValidationResult(false, fieldName + " must be less than 255 characters");
        }
        
        // Check if at least one skill is provided (comma-separated)
        String[] skillArray = skills.split(",");
        if (skillArray.length == 0 || skillArray[0].trim().isEmpty()) {
            return new ValidationResult(false, "Please provide at least one skill");
        }
        
        return new ValidationResult(true, "Valid skills");
    }
    
    /**
     * Validates bio/description field
     * @param bio Bio to validate
     * @return ValidationResult with status and message
     */
    public static ValidationResult validateBio(String bio) {
        if (bio == null || bio.trim().isEmpty()) {
            return new ValidationResult(true, "Bio is optional");
        }
        
        if (bio.trim().length() > 500) {
            return new ValidationResult(false, "Bio must be less than 500 characters");
        }
        
        return new ValidationResult(true, "Valid bio");
    }
    
    /**
     * Result class for validation operations
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
