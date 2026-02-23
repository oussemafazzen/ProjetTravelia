package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.InputStream;

public class TestController {

    @FXML private ImageView heroImage;

    @FXML
    public void initialize() {
        // ===== load image from resources =====
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("images/img_acceuil.jpg")) {

            if (is != null && heroImage != null) {
                heroImage.setImage(new Image(is));
            } else {
                System.out.println("Image NOT FOUND: src/main/resources/images/img_acceuil.jpg");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ===== rounded corners =====
        if (heroImage != null) {
            Rectangle clip = new Rectangle(620, 340);
            clip.setArcWidth(40);
            clip.setArcHeight(40);
            heroImage.setClip(clip);
        }
    }

    @FXML private void goHome(ActionEvent e) { switchScene(e, "/fxml/test.fxml"); }
    @FXML private void goClients(ActionEvent e) { switchScene(e, "/fxml/clients.fxml"); }
    @FXML private void goReservations(ActionEvent e) { switchScene(e, "/fxml/reservations.fxml"); }
    @FXML private void goHebergement(ActionEvent e) { switchScene(e, "/fxml/hebergement.fxml"); }
    @FXML private void goActivites(ActionEvent e) { switchScene(e, "/fxml/activites.fxml"); }
    @FXML private void goAvis(ActionEvent e) { switchScene(e, "/fxml/avis.fxml"); }

    private void switchScene(ActionEvent e, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            // ✅ keep same window size دائما
            double w = stage.getWidth();
            double h = stage.getHeight();

            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.toString()).showAndWait();
        }
    }
}