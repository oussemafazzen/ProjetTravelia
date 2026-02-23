package org.example.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.SessionContext;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        SessionContext.loginAsClient(1); // mets un id qui existe dans ta table reservation.id_client
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/my_reservations.fxml"));

        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        stage.setTitle("Travelia");
        stage.setScene(scene);

        stage.setMinWidth(1300);
        stage.setMinHeight(800);
        stage.setMaxWidth(1300);
        stage.setMaxHeight(800);

        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}