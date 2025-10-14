package com.unieats.controllers;

import com.unieats.DatabaseManager;
import com.unieats.User;
import com.unieats.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class SignupController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private HBox passwordRow;
    @FXML private HBox confirmPasswordRow;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> userCategoryComboBox;
    @FXML private Button signupButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Button passwordToggleButton;
    @FXML private Button confirmPasswordToggleButton;
    @FXML private Label passwordValidationLabel;
    @FXML private Label confirmPasswordValidationLabel;
    @FXML private org.kordamp.ikonli.javafx.FontIcon passwordToggleIcon;
    @FXML private org.kordamp.ikonli.javafx.FontIcon confirmPasswordToggleIcon;
    
    private DatabaseManager dbManager;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    private TextField passwordTextField;
    private TextField confirmPasswordTextField;
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupValidation();
        setupErrorLabel();
        setupUserCategoryComboBox();
    }
    
    private void setupUserCategoryComboBox() {
        userCategoryComboBox.getItems().addAll("Student", "Seller");
        userCategoryComboBox.setValue("Student"); // Default selection
    }
    
    private void setupValidation() {
        // Real-time validation for email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !isValidEmail(newValue)) {
                emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            } else {
                emailField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
            }
        });
        
        // Enhanced password validation
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword(newValue);
            // Also update confirm password validation
            validateConfirmPassword(confirmPasswordField.getText());
        });
        
        // Confirm password validation
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateConfirmPassword(newValue);
        });
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            passwordValidationLabel.setVisible(false);
            passwordField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
            return;
        }

        StringBuilder errors = new StringBuilder();
        
        if (password.length() <= 5) {
            errors.append("• Must be at least 6 characters long\n");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.append("• Must contain at least 1 uppercase letter\n");
        }
        
        if (!password.matches(".*[a-z].*")) {
            errors.append("• Must contain at least 1 lowercase letter\n");
        }
        
        if (!password.matches(".*\\d.*")) {
            errors.append("• Must contain at least 1 digit\n");
        }
        
        if (!password.matches(".*[@$!%*?&].*")) {
            errors.append("• Must contain at least 1 special character (@$!%*?&)");
        }

        if (errors.length() > 0) {
            passwordValidationLabel.setText(errors.toString().trim());
            passwordValidationLabel.setVisible(true);
            passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            passwordValidationLabel.setVisible(false);
            passwordField.setStyle("-fx-border-color: #10b981; -fx-border-width: 2;");
        }
    }

    private void validateConfirmPassword(String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            confirmPasswordValidationLabel.setVisible(false);
            confirmPasswordField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
            return;
        }

        if (!confirmPassword.equals(passwordField.getText())) {
            confirmPasswordValidationLabel.setText("• Passwords do not match");
            confirmPasswordValidationLabel.setVisible(true);
            confirmPasswordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            confirmPasswordValidationLabel.setVisible(false);
            confirmPasswordField.setStyle("-fx-border-color: #10b981; -fx-border-width: 2;");
        }
    }
    
    private void setupErrorLabel() {
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleSignup() {
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validate all fields
        if (!validateFields()) {
            return;
        }
        
        // Check if email already exists
        if (dbManager.isEmailExists(emailField.getText().trim())) {
            showError("Email already exists. Please use a different email address.");
            return;
        }
        
        // Create new user with hashed password and user category
        User newUser = new User(
            emailField.getText().trim(),
            PasswordUtil.hashPassword(passwordField.getText()),
            fullNameField.getText().trim(),
            userCategoryComboBox.getValue().toLowerCase()
        );
        
        // Save to database
        if (dbManager.createUser(newUser)) {
            showSuccess("Account created successfully! Welcome to UniEats!");
            // Navigate back to home page after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::navigateToHome);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            showError("Failed to create account. Please try again.");
        }
    }
    
    private boolean validateFields() {
        // Email validation
        if (emailField.getText() == null || !isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address.");
            return false;
        }
        
        // Password validation
        if (passwordField.getText() == null || passwordField.getText().length() < 6) {
            showError("Password must be at least 6 characters long.");
            return false;
        }
        
        // Confirm password validation
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match.");
            return false;
        }
        
        // Full name validation
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required.");
            return false;
        }
        
        // User category validation
        if (userCategoryComboBox.getValue() == null) {
            showError("Please select a user category.");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        errorLabel.setTextFill(Color.GREEN);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    @FXML
    private void handleBack() {
        navigateToHome();
    }
    
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            // Get the current stage from any scene in the current window
            Stage stage = null;
            if (fullNameField != null && fullNameField.getScene() != null) {
                stage = (Stage) fullNameField.getScene().getWindow();
            } else if (signupButton != null && signupButton.getScene() != null) {
                stage = (Stage) signupButton.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                
                stage.setScene(scene);
                stage.show();
            } else {
                System.err.println("Could not find current stage for navigation");
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to home: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordRow == null) return;
        if (passwordVisible) {
            passwordToggleIcon.setIconLiteral("fas-eye-slash");
            if (passwordTextField == null) {
                passwordTextField = new TextField();
                passwordTextField.setPromptText(passwordField.getPromptText());
                passwordTextField.setStyle(passwordField.getStyle());
                passwordTextField.setPrefWidth(passwordField.getPrefWidth());
                passwordTextField.setPrefHeight(passwordField.getPrefHeight());
                passwordTextField.setFont(passwordField.getFont());
                passwordTextField.textProperty().addListener((obs, o, n) -> passwordField.setText(n));
            }
            passwordTextField.setText(passwordField.getText());
            passwordRow.getChildren().set(0, passwordTextField);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordToggleIcon.setIconLiteral("fas-eye");
            passwordField.setText(passwordTextField != null ? passwordTextField.getText() : passwordField.getText());
            passwordRow.getChildren().set(0, passwordField);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        
        if (confirmPasswordRow == null) return;
        if (confirmPasswordVisible) {
            confirmPasswordToggleIcon.setIconLiteral("fas-eye-slash");
            if (confirmPasswordTextField == null) {
                confirmPasswordTextField = new TextField();
                confirmPasswordTextField.setPromptText(confirmPasswordField.getPromptText());
                confirmPasswordTextField.setStyle(confirmPasswordField.getStyle());
                confirmPasswordTextField.setPrefWidth(confirmPasswordField.getPrefWidth());
                confirmPasswordTextField.setPrefHeight(confirmPasswordField.getPrefHeight());
                confirmPasswordTextField.setFont(confirmPasswordField.getFont());
                confirmPasswordTextField.textProperty().addListener((obs, o, n) -> confirmPasswordField.setText(n));
            }
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordRow.getChildren().set(0, confirmPasswordTextField);
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
        } else {
            confirmPasswordToggleIcon.setIconLiteral("fas-eye");
            confirmPasswordField.setText(confirmPasswordTextField != null ? confirmPasswordTextField.getText() : confirmPasswordField.getText());
            confirmPasswordRow.getChildren().set(0, confirmPasswordField);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
        }
    }

    @FXML
    private void handleClear() {
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameField.clear();
        userCategoryComboBox.setValue("Student");
        errorLabel.setVisible(false);
        passwordValidationLabel.setVisible(false);
        confirmPasswordValidationLabel.setVisible(false);
        
        // Reset field styles
        emailField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
        passwordField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
        confirmPasswordField.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 1;");
    }
} 