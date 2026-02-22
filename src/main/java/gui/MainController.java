package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Button btnTheme;
    @FXML
    private VBox sideBar;

    private boolean isDarkTheme = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showHebergement();
    }

    @FXML
    private void toggleSidebar() {
        if (sideBar.isVisible()) {
            sideBar.setVisible(false);
            sideBar.setManaged(false);
        } else {
            sideBar.setVisible(true);
            sideBar.setManaged(true);
        }
    }

    @FXML
    private void toggleTheme() {
        Scene scene = mainPane.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        if (isDarkTheme) {
            scene.getStylesheets().add(getClass().getResource("/light-theme.css").toExternalForm());
            btnTheme.setText("☀️ Mode Clair");
            isDarkTheme = false;
        } else {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            btnTheme.setText("🌑 Mode Sombre");
            isDarkTheme = true;
        }
    }

    @FXML
    private void showClients() {
        loadView("/views/PlaceholderView.fxml", "Clients & Préférences");
    }

    @FXML
    private void showReservations() {
        loadView("/views/PlaceholderView.fxml", "Réservations & Billets");
    }

    @FXML
    private void showHebergement() {
        try {
            VBox view = FXMLLoader.load(getClass().getResource("/views/HebergementView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void showReservationHebergement() {
        try {
            VBox view = FXMLLoader.load(getClass().getResource("/views/ReservationHebergementView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void showActivites() {
        loadView("/views/PlaceholderView.fxml", "Activités Touristiques");
    }

    @FXML
    private void showAvis() {
        loadView("/views/PlaceholderView.fxml", "Avis & Recommandations");
    }

    private void loadView(String fxmlPath, String moduleName) {
        try {
            VBox view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainPane.setCenter(view);
        } catch (IOException e) {
            System.out.println("Module " + moduleName + " - En cours de développement");
        }
    }
}
