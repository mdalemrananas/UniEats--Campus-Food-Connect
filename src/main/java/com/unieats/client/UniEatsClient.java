package com.unieats.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UniEatsClient extends Application {
    private static ClientHandler clientHandler;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize client handler and connect to server
            clientHandler = new ClientHandler("localhost", 5000);
            clientHandler.start();

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setClientHandler(clientHandler);

            Scene scene = new Scene(root, 360, 640);
            primaryStage.setTitle("UniEats Client");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ClientHandler getClientHandler() {
        return clientHandler;
    }
}
