package org.example.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Charger le dashboard admin
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(
                        getClass().getResource("/fxml/MainView.fxml"),
                        "Fichier introuvable: /fxml/MainView.fxml"
                )
        );

        Scene scene = new Scene(root, 1400, 850);

        // Charger le CSS ADMIN
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/css/admin.css"),
                        "Fichier introuvable: /css/admin.css"
                ).toExternalForm()
        );

        primaryStage.setTitle("Travelia - Backoffice Admin");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // ouvre en plein écran
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}