package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Activite;
import models.Avis;
import models.Hebergement;
import models.PhotoAvis;
import services.ActiviteService;
import services.AvisService;
import services.HebergementService;
import utils.SessionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AvisFormController {

    @FXML private ComboBox<String> cbTypeService;
    @FXML private TextField tfIdService;
    @FXML private TextArea taCommentaire;
    @FXML private Label lblPhotoPath;
    @FXML private StackPane photoPreviewContainer;

    @FXML private Label star1, star2, star3, star4, star5;
    @FXML private Label lblNoteValue;

    private int selectedNote = 0;
    private File selectedPhotoFile;
    private final AvisService avisService = new AvisService();
    private final HebergementService hebergementService = new HebergementService();
    private final ActiviteService activiteService = new ActiviteService();
    private AvisFrontController parentController;

    @FXML
    public void initialize() {
        // Init Service Types
        cbTypeService.setItems(FXCollections.observableArrayList("Hébergement", "Activité", "Transport"));

        // Setup Star Rating System
        setupStar(star1, 1);
        setupStar(star2, 2);
        setupStar(star3, 3);
        setupStar(star4, 4);
        setupStar(star5, 5);
        
        // Listen to combo box changes to update prompt text
        cbTypeService.setOnAction(e -> {
            String type = cbTypeService.getValue();
            if ("Transport".equals(type)) {
                tfIdService.setPromptText("Ex: Avion, Bus, Train");
            } else {
                tfIdService.setPromptText("Ex: Hôtel Regency / Parachutisme");
            }
        });
    }

    public void setParentController(AvisFrontController parent) {
        this.parentController = parent;
    }

    private void setupStar(Label star, int value) {
        star.setOnMouseClicked(event -> {
            selectedNote = value;
            updateStars(selectedNote);
            lblNoteValue.setText("(" + selectedNote + "/5)");
        });

        star.setOnMouseEntered(event -> updateStars(value));
        star.setOnMouseExited(event -> updateStars(selectedNote));
    }

    private void updateStars(int note) {
        Label[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < 5; i++) {
            if (i < note) {
                stars[i].setText("★");
            } else {
                stars[i].setText("☆");
            }
        }
    }

    @FXML
    void handleChoosePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedPhotoFile = file;
            lblPhotoPath.setText(file.getName());
            photoPreviewContainer.setVisible(true);
            photoPreviewContainer.setManaged(true);
        }
    }

    @FXML
    void handlePublish(ActionEvent event) {
        // 1. Validation
        if (selectedNote == 0) {
            showAlert("Erreur", "Veuillez sélectionner une note entre 1 et 5 étoiles.");
            return;
        }
        String typeService = cbTypeService.getValue();
        if (typeService == null) {
            showAlert("Erreur", "Veuillez choisir le type de service.");
            return;
        }
        String serviceInput = tfIdService.getText().trim();
        if (serviceInput.isEmpty()) {
            showAlert("Erreur", "Veuillez renseigner le " + ("Transport".equals(typeService) ? "type de transport" : "nom du service") + ".");
            return;
        }
        if (taCommentaire.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez écrire un commentaire.");
            return;
        }

        int idServiceToSave = 0;
        
        // 2. Lookup Logic based on requirement
        try {
            if ("Hébergement".equalsIgnoreCase(typeService)) {
                idServiceToSave = findHebergementIdByName(serviceInput);
                if (idServiceToSave == -1) {
                    showAlert("Hébergement introuvable", "L'hébergement '" + serviceInput + "' n'existe pas dans la base de données.");
                    return;
                }
            } else if ("Activité".equalsIgnoreCase(typeService)) {
                idServiceToSave = findActiviteIdByName(serviceInput);
                if (idServiceToSave == -1) {
                    showAlert("Activité introuvable", "L'activité '" + serviceInput + "' n'existe pas dans la base de données.");
                    return;
                }
            } else if ("Transport".equalsIgnoreCase(typeService)) {
                // For transport, we map type strings to IDs or use 1 for Avion, 2 for Bus, etc.
                // Or just set it to a dummy value if no table exists.
                String t = serviceInput.toLowerCase();
                if (t.contains("avion")) idServiceToSave = 1;
                else if (t.contains("bus")) idServiceToSave = 2;
                else if (t.contains("train")) idServiceToSave = 3;
                else idServiceToSave = 4; // Generic transport
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur Database", "Erreur lors de la vérification du service : " + e.getMessage());
            return;
        }

        // 3. Prepare Data
        Integer currentClientId = SessionContext.getCurrentUserId();
        if (currentClientId == null) {
            showAlert("Erreur", "Utilisateur non connecté.");
            return;
        }

        Avis newAvis = new Avis();
        newAvis.setCommentaire(taCommentaire.getText().trim());
        newAvis.setNote(selectedNote);
        newAvis.setTypeService(typeService);
        newAvis.setIdService(idServiceToSave);
        newAvis.setIdClient(currentClientId);

        // Add Photo if selected
        if (selectedPhotoFile != null) {
            String fileName = System.currentTimeMillis() + "_" + selectedPhotoFile.getName();
            File destFolder = new File("src/main/resources/images/avis/");
            if (!destFolder.exists()) destFolder.mkdirs();
            
            File destFile = new File(destFolder, fileName);
            try {
                Files.copy(selectedPhotoFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                List<PhotoAvis> photos = new ArrayList<>();
                PhotoAvis photo = new PhotoAvis();
                photo.setCheminFichier("/images/avis/" + fileName); // Store relative path
                photo.setLegende("Photo jointe à l'avis");
                photos.add(photo);
                newAvis.setPhotos(photos);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur Fichier", "Impossible de copier la photo : " + e.getMessage());
                return;
            }
        }

        // 4. Save to DB
        try {
            avisService.add(newAvis);
            if (parentController != null) {
                parentController.initialize();
            }
            handleCancel(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Impossible d'enregistrer l'avis : " + e.getMessage());
        }
    }

    private int findHebergementIdByName(String name) throws SQLException {
        List<Hebergement> list = hebergementService.recupTousHebergements();
        for (Hebergement h : list) {
            if (h.getNom().equalsIgnoreCase(name)) return h.getIdHebergement();
        }
        return -1;
    }

    private int findActiviteIdByName(String name) throws SQLException {
        List<Activite> list = activiteService.recupToutesActivites();
        for (Activite a : list) {
            if (a.getNom().equalsIgnoreCase(name)) return a.getIdActivite();
        }
        return -1;
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
