package com.unieats.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private Button loginButton;

    private ClientHandler clientHandler;

    public void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_menu.fxml"));
                Parent root = loader.load();
                ClientMenuController controller = loader.getController();
                controller.setClientHandler(clientHandler);
                controller.setUsername(username);
                controller.loadMenuItems();  // Initial load from server

                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root, 360, 640);
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
