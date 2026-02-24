package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Application Start Method Called");
        try {
            // Utilisation robuste de getClass().getResource
            java.net.URL fxmlLocation = getClass().getResource("/fxml/Home.fxml");
            
            if (fxmlLocation == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier FXML !");
                System.err.println("Vérifiez que le fichier est bien dans src/main/resources/fxml/Home.fxml");
                return;
            }
            
            System.out.println("Fichier FXML trouvé : " + fxmlLocation);
            
            // Session Persistence / Auto-login logic
            String savedEmail = utils.SessionManager.loadSession();
            if (savedEmail != null) {
                System.out.println("Session trouvée pour : " + savedEmail);
                services.UserService userService = new services.UserService();
                models.User user = userService.getUserByEmail(savedEmail);
                
                if (user != null) {
                    String destinationFxml = "";
                    if (user.getRole() == models.enums.Role.ADMINISTRATEUR) {
                        destinationFxml = "/fxml/AdminPanel.fxml";
                    } else {
                        destinationFxml = "/fxml/Dashboard.fxml";
                    }
                    
                    System.out.println("Auto-login vers : " + destinationFxml);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(destinationFxml));
                    Parent root = loader.load();
                    
                    if (destinationFxml.equals("/fxml/Dashboard.fxml") && user instanceof models.Client) {
                        controllers.DashboardController dc = loader.getController();
                        dc.initData((models.Client) user);
                    }
                    
                    Scene scene = new Scene(root);
                    primaryStage.setTitle("Travelia - " + (user.getRole() == models.enums.Role.ADMINISTRATEUR ? "Admin" : "Dashboard"));
                    primaryStage.setScene(scene);
                    primaryStage.setMaximized(true);
                    primaryStage.setResizable(true);
                    primaryStage.show();
                    return; // Bypass normal Home.fxml loading
                }
            }

            System.out.println("Chargement du FXML Accueil...");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion Utilisateur - Accueil");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            
        } catch (Throwable e) {
            System.err.println("ERREUR CRITIQUE lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
