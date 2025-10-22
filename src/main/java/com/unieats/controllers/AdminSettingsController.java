package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.unieats.DatabaseManager;
import com.unieats.User;
import com.unieats.util.PasswordUtil;

public class AdminSettingsController {

    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileEmailInfoLabel;
    @FXML private Label profileCategoryLabel;
    @FXML private ImageView profileImageView;
    @FXML private Button editProfileButton;
    @FXML private Button changePasswordButton;
    @FXML private Button logoutButton;

    private DatabaseManager dbManager;
    private User currentUser;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        currentUser = DatabaseManager.getCurrentUser();
        if (currentUser == null) {
            // fall back: try admin by role if needed (optional)
        }
        displayUserInfo();
        wireActions();
    }

    private void wireActions() {
        if (editProfileButton != null) editProfileButton.setOnAction(e -> handleEditProfile());
        if (changePasswordButton != null) changePasswordButton.setOnAction(e -> handleChangePassword());
        if (logoutButton != null) logoutButton.setOnAction(e -> handleLogout());
    }

    private void displayUserInfo() {
        if (currentUser == null) return;
        if (profileNameLabel != null) profileNameLabel.setText(currentUser.getFullName());
        if (profileEmailLabel != null) profileEmailLabel.setText(currentUser.getEmail());
        if (profileEmailInfoLabel != null) profileEmailInfoLabel.setText(currentUser.getEmail());
        if (profileCategoryLabel != null) profileCategoryLabel.setText("ADMIN");
        if (profileImageView != null) {
            String path = currentUser.getProfilePicture();
            if (path != null && !path.isEmpty()) {
                try {
                    String resourcePath = path.startsWith("images/") ? "/" + path : "/" + path;
                    try {
                        Image img = new Image(getClass().getResourceAsStream(resourcePath), 110, 110, true, true);
                        profileImageView.setImage(img);
                    } catch (Exception e) {
                        File f = new File(path);
                        if (f.exists()) {
                            profileImageView.setImage(new Image(f.toURI().toString(), 110, 110, true, true));
                        } else {
                            String userDir = System.getProperty("user.dir");
                            Path imagePath = Paths.get(userDir, "src", "main", "resources", path);
                            if (Files.exists(imagePath)) {
                                profileImageView.setImage(new Image(imagePath.toUri().toString(), 110, 110, true, true));
                            } else {
                                profileImageView.setImage(null);
                            }
                        }
                    }
                } catch (Exception ignored) { profileImageView.setImage(null); }
            } else {
                profileImageView.setImage(null);
            }
        }
    }

    private void handleEditProfile() {
        if (currentUser == null) { showAlert("Error", "No admin is signed in."); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin Profile");
        dialog.setHeaderText("Update admin details");
        dialog.getDialogPane().setPrefWidth(360);

        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Full name");
        Label emailStatic = new Label(currentUser.getEmail());
        emailStatic.setStyle("-fx-text-fill: #6c757d;");
        TextField phoneField = new TextField(currentUser.getPhoneNo() == null ? "" : currentUser.getPhoneNo());
        phoneField.setPromptText("Phone no");
        TextArea addressArea = new TextArea(currentUser.getAddress() == null ? "" : currentUser.getAddress());
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(3);

        ImageView preview = new ImageView();
        preview.setFitWidth(80);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);
        String existingPic = currentUser.getProfilePicture();
        if (existingPic != null && !existingPic.isEmpty()) {
            try {
                String resourcePath = existingPic.startsWith("images/") ? "/" + existingPic : "/" + existingPic;
                try {
                    preview.setImage(new Image(getClass().getResourceAsStream(resourcePath), 80, 80, true, true));
                } catch (Exception e) {
                    File ef = new File(existingPic);
                    if (ef.exists()) {
                        preview.setImage(new Image(ef.toURI().toString(), 80, 80, true, true));
                    } else {
                        String userDir = System.getProperty("user.dir");
                        Path imagePath = Paths.get(userDir, "src", "main", "resources", existingPic);
                        if (Files.exists(imagePath)) {
                            preview.setImage(new Image(imagePath.toUri().toString(), 80, 80, true, true));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        Button choosePicBtn = new Button("Choose...");
        final String[] selectedPicPath = { existingPic };
        choosePicBtn.setOnAction(ev2 -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Profile Picture");
            chooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            Stage stage = (Stage) editProfileButton.getScene().getWindow();
            File selected = chooser.showOpenDialog(stage);
            if (selected != null) {
                selectedPicPath[0] = selected.getAbsolutePath();
                try { preview.setImage(new Image(selected.toURI().toString(), 80, 80, true, true)); } catch (Exception ignored) {}
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Profile picture:"), 0, 0);
        grid.add(preview, 1, 0);
        grid.add(choosePicBtn, 1, 1);
        grid.add(new Label("Full name:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailStatic, 1, 3);
        grid.add(new Label("Phone no:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("Address:"), 0, 5);
        grid.add(addressArea, 1, 5);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String fullName = nameField.getText() != null ? nameField.getText().trim() : "";
            if (fullName.isEmpty()) {
                ev.consume();
                showAlert("Validation Error", "Full name is required.");
                return;
            }
            String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String addr = addressArea.getText() != null ? addressArea.getText().trim() : "";
            try {
                currentUser.setFullName(fullName);
                currentUser.setPhoneNo(phone.isEmpty() ? null : phone);
                currentUser.setAddress(addr.isEmpty() ? null : addr);
                boolean ok = dbManager.updateUser(currentUser);
                if (!ok) {
                    ev.consume();
                    showAlert("Update Failed", "Could not update profile. Please try again.");
                    return;
                }
                String newPic = selectedPicPath[0];
                String oldPic = existingPic;
                if ((newPic != null && !newPic.isEmpty()) && (oldPic == null || !newPic.equals(oldPic))) {
                    try {
                        String userDir = System.getProperty("user.dir");
                        Path targetDir = Paths.get(userDir, "src", "main", "resources", "images");
                        Files.createDirectories(targetDir);
                        String originalFilename = new File(newPic).getName();
                        String fileExtension = "";
                        int lastDot = originalFilename.lastIndexOf('.');
                        if (lastDot > 0) { fileExtension = originalFilename.substring(lastDot); }
                        String newFilename = "profile_" + currentUser.getId() + "_" + UUID.randomUUID() + fileExtension;
                        Path sourcePath = Paths.get(newPic);
                        Path targetPath = targetDir.resolve(newFilename);
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        String relativePath = "images/" + newFilename;
                        boolean picOk = dbManager.updateUserProfilePicture(currentUser.getId(), relativePath);
                        if (!picOk) {
                            ev.consume();
                            showAlert("Update Failed", "Profile updated but picture change failed.");
                            return;
                        }
                        currentUser.setProfilePicture(relativePath);
                    } catch (Exception ex) {
                        ev.consume();
                        showAlert("Error", "Failed to save profile picture: " + ex.getMessage());
                        return;
                    }
                }
            } catch (Exception ex) {
                ev.consume();
                showAlert("Error", "Failed to update profile: " + ex.getMessage());
                return;
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                User refreshed = dbManager.getUserByEmail(currentUser.getEmail());
                if (refreshed != null) { currentUser = refreshed; }
                displayUserInfo();
                showAlert("Success", "Profile updated successfully.");
            }
        });
    }

    private void handleChangePassword() {
        if (currentUser == null) { showAlert("Error", "No admin is signed in."); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Update your password");

        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Current password");
        PasswordField newField = new PasswordField();
        newField.setPromptText("New password (min 6 chars)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");
        Button toggleCurrent = new Button(); toggleCurrent.setGraphic(new FontIcon("fas-eye"));
        Button toggleNew = new Button(); toggleNew.setGraphic(new FontIcon("fas-eye"));
        Button toggleConfirm = new Button(); toggleConfirm.setGraphic(new FontIcon("fas-eye"));
        TextField currentText = new TextField(); currentText.setManaged(false); currentText.setVisible(false);
        TextField newText = new TextField(); newText.setManaged(false); newText.setVisible(false);
        TextField confirmText = new TextField(); confirmText.setManaged(false); confirmText.setVisible(false);
        currentText.textProperty().bindBidirectional(currentField.textProperty());
        newText.textProperty().bindBidirectional(newField.textProperty());
        confirmText.textProperty().bindBidirectional(confirmField.textProperty());
        toggleCurrent.setOnAction(e -> toggleFieldVisibility(currentField, currentText, (FontIcon) toggleCurrent.getGraphic()));
        toggleNew.setOnAction(e -> toggleFieldVisibility(newField, newText, (FontIcon) toggleNew.getGraphic()));
        toggleConfirm.setOnAction(e -> toggleFieldVisibility(confirmField, confirmText, (FontIcon) toggleConfirm.getGraphic()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Current:"), 0, 0);
        grid.add(new HBox(6, currentField, currentText, toggleCurrent), 1, 0);
        grid.add(new Label("New:"), 0, 1);
        grid.add(new HBox(6, newField, newText, toggleNew), 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(new HBox(6, confirmField, confirmText, toggleConfirm), 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        Button changeBtn = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        changeBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String current = currentField.getText();
            String next = newField.getText();
            String confirm = confirmField.getText();
            if (current == null || current.isEmpty()) { ev.consume(); showAlert("Validation Error", "Enter your current password."); return; }
            if (!PasswordUtil.verifyPassword(current, currentUser.getPassword())) { ev.consume(); showAlert("Invalid Password", "Current password is incorrect."); return; }
            if (next == null || next.length() < 6) { ev.consume(); showAlert("Validation Error", "New password must be at least 6 characters."); return; }
            if (!next.equals(confirm)) { ev.consume(); showAlert("Validation Error", "New password and confirmation do not match."); return; }
            try {
                currentUser.setPassword(PasswordUtil.hashPassword(next));
                boolean ok = dbManager.updateUser(currentUser);
                if (!ok) { ev.consume(); showAlert("Update Failed", "Could not change password. Please try again."); }
            } catch (Exception ex) { ev.consume(); showAlert("Error", "Failed to change password: " + ex.getMessage()); }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == changeButtonType) {
                User refreshed = dbManager.getUserByEmail(currentUser.getEmail());
                if (refreshed != null) currentUser = refreshed;
                displayUserInfo();
                showAlert("Success", "Password changed successfully.");
            }
        });
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to sign in again.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                    stage.setScene(scene);
                    stage.setTitle("UniEats - Sign In");
                    stage.show();
                } catch (IOException e) {
                    showAlert("Navigation Error", "Failed to navigate to sign in page: " + e.getMessage());
                }
            }
        });
    }

    private void toggleFieldVisibility(PasswordField pwd, TextField txt, FontIcon icon) {
        boolean show = !txt.isVisible();
        txt.setManaged(show);
        txt.setVisible(show);
        pwd.setManaged(!show);
        pwd.setVisible(!show);
        icon.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
        if (show) { txt.requestFocus(); txt.positionCaret(txt.getText() != null ? txt.getText().length() : 0); }
        else { pwd.requestFocus(); pwd.positionCaret(pwd.getText() != null ? pwd.getText().length() : 0); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

