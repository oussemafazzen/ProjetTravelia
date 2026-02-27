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

        SessionContext.loginAsClient(1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/test.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1300, 800);

        var cssUrl = getClass().getResource("/css/app.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

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
        launch(args);
    }
}