package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Hebergement;
import services.HebergementService;

import java.io.IOException;
import java.net.URL;
import javafx.collections.transformation.FilteredList;
import java.util.ResourceBundle;
import utils.PdfExporter;
import java.util.function.Function;

public class HebergementController implements Initializable {

<<<<<<< HEAD
    @FXML
    private TableView<Hebergement> tableHebergement;
    @FXML
    private TableColumn<Hebergement, Integer> colId;
    @FXML
    private TableColumn<Hebergement, String> colNom;
    @FXML
    private TableColumn<Hebergement, String> colType;
    @FXML
    private TableColumn<Hebergement, String> colAdresse;
    @FXML
    private TableColumn<Hebergement, String> colVille;
    @FXML
    private TableColumn<Hebergement, String> colPays;
    @FXML
    private TableColumn<Hebergement, Integer> colCapacite;
    @FXML
    private TableColumn<Hebergement, String> colEquipements;
    @FXML
    private TableColumn<Hebergement, Double> colTarif;
    @FXML
    private TextField tfSearch;
    @FXML
    private Label lblTotal;
=======
    @FXML private TableView<Hebergement> tableHebergement;
    @FXML private TableColumn<Hebergement, Integer> colId;
    @FXML private TableColumn<Hebergement, String>  colNom;
    @FXML private TableColumn<Hebergement, String>  colType;
    @FXML private TableColumn<Hebergement, String>  colAdresse;
    @FXML private TableColumn<Hebergement, String>  colVille;
    @FXML private TableColumn<Hebergement, String>  colPays;
    @FXML private TableColumn<Hebergement, Integer> colCapacite;
    @FXML private TableColumn<Hebergement, String>  colEquipements;
    @FXML private TableColumn<Hebergement, Double>  colTarif;
    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322

    private HebergementService hs = new HebergementService();
    private ObservableList<Hebergement> hebergementList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idHebergement"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEquipements.setCellValueFactory(new PropertyValueFactory<>("equipements"));
        colTarif.setCellValueFactory(new PropertyValueFactory<>("tarifParNuit"));

<<<<<<< HEAD
        // ── Custom Cell Factories (The "Do it yourself" logic) ─────────────
=======
        // ── Custom Cell Factories ──────────────────────────────────────────
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322

        // Type badge (hotel → blue pill, auberge → green pill)
        colType.setCellFactory(col -> new TableCell<Hebergement, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    String lower = type.toLowerCase();
                    if (lower.contains("hotel") || lower.contains("hôtel")) {
                        badge.setText("🏨  " + type);
                        badge.getStyleClass().add("badge-hotel");
                    } else if (lower.contains("auberge")) {
                        badge.setText("🏕️  " + type);
                        badge.getStyleClass().add("badge-auberge");
                    } else {
                        badge.setText(type);
                        badge.getStyleClass().add("badge-default");
                    }
<<<<<<< HEAD
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(badge);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
=======
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Tarif price tag
        colTarif.setCellFactory(col -> new TableCell<Hebergement, Double>() {
            @Override
            protected void updateItem(Double tarif, boolean empty) {
                super.updateItem(tarif, empty);
                if (empty || tarif == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label tag = new Label(String.format("💰 %.0f DT", tarif));
                    tag.getStyleClass().add("price-tag");
<<<<<<< HEAD
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(tag);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
=======
                    HBox box = new HBox(tag);
                    box.setAlignment(Pos.CENTER_LEFT);
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        loadData();
    }

    public void loadData() {
        hebergementList = FXCollections.observableArrayList(hs.getAll());
<<<<<<< HEAD
        
=======

>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322
        // Update stats label
        if (lblTotal != null) {
            lblTotal.setText(String.valueOf(hebergementList.size()));
        }

        // Wrap the observable list in a FilteredList
        FilteredList<Hebergement> filteredData = new FilteredList<>(hebergementList, p -> true);

        // Add a listener to tfSearch to update the filter predicate
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(hebergement -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                
                boolean matchesNom = hebergement.getNom() != null && hebergement.getNom().toLowerCase().contains(lowerCaseFilter);
                boolean matchesPays = hebergement.getPays() != null && hebergement.getPays().toLowerCase().contains(lowerCaseFilter);
                
                return matchesNom || matchesPays;
            });
            // Update count after filter
            if (lblTotal != null) {
                lblTotal.setText(String.valueOf(filteredData.size()));
            }
        });

        tableHebergement.setItems(filteredData);
    }

    @FXML
    private void handleSortByType() {
        if (hebergementList != null) {
<<<<<<< HEAD
            FXCollections.sort(hebergementList, (h1, h2) -> {
                String t1 = h1.getType() != null ? h1.getType() : "";
                String t2 = h2.getType() != null ? h2.getType() : "";
                return t1.compareToIgnoreCase(t2);
            });
=======
            FXCollections.sort(hebergementList, (h1, h2) -> h1.getType().compareToIgnoreCase(h2.getType()));
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322
        }
    }

    @FXML
    private void handleSortByVille() {
        if (hebergementList != null) {
<<<<<<< HEAD
            FXCollections.sort(hebergementList, (h1, h2) -> {
                String v1 = h1.getVille() != null ? h1.getVille() : "";
                String v2 = h2.getVille() != null ? h2.getVille() : "";
                return v1.compareToIgnoreCase(v2);
            });
=======
            FXCollections.sort(hebergementList, (h1, h2) -> h1.getVille().compareToIgnoreCase(h2.getVille()));
>>>>>>> 1eb3045ff5a423da6e22dc4ea84ae9b1bb5e5322
        }
    }

    @FXML
    private void handleAdd() {
        openModal(null);
    }

    @FXML
    private void handleUpdate() {
        Hebergement selected = tableHebergement.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openModal(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un hébergement à modifier.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Hebergement selected = tableHebergement.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cet hébergement ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                hs.delete(selected);
                loadData();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleExportPdf() {
        String[] headers = {"ID", "Nom", "Type", "Ville", "Pays", "Capacité", "Tarif"};
        Function<Hebergement, String>[] extractors = new Function[]{
            (Function<Hebergement, String>) h -> String.valueOf(h.getIdHebergement()),
            (Function<Hebergement, String>) h -> h.getNom(),
            (Function<Hebergement, String>) h -> h.getType(),
            (Function<Hebergement, String>) h -> h.getVille(),
            (Function<Hebergement, String>) h -> h.getPays(),
            (Function<Hebergement, String>) h -> String.valueOf(h.getCapacite()),
            (Function<Hebergement, String>) h -> String.valueOf(h.getTarifParNuit()) + " DT"
        };

        PdfExporter.exportToPdf(
            "Liste des Hébergements",
            "Hebergements_List.pdf",
            tableHebergement.getItems(),
            headers,
            extractors
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText(null);
        alert.setContentText("Le fichier PDF a été généré sur votre Bureau : Hebergements_List.pdf");
        alert.show();
    }

    @FXML
    private void handleStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HebergementStatsView.fxml"));
            Parent root = loader.load();

            HebergementStatsController controller = loader.getController();
            controller.setData(tableHebergement.getItems());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Statistiques par Ville");

            Scene scene = new Scene(root);
            if (tableHebergement.getScene() != null && !tableHebergement.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableHebergement.getScene().getStylesheets().get(0));
            } else {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur chargement stats: " + e.getMessage());
        }
    }

    private void openModal(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HebergementForm.fxml"));
            Parent root = loader.load();

            HebergementFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setHebergement(hebergement);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(hebergement == null ? "Ajouter un Hébergement" : "Modifier l'Hébergement");

            Scene scene = new Scene(root);
            if (tableHebergement.getScene() != null && !tableHebergement.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableHebergement.getScene().getStylesheets().get(0));
            } else {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur chargement modal: " + e.getMessage());
        }
    }
}
