package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import models.Activite;
import services.ActiviteService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Function;
import utils.PdfExporter;

public class ActiviteController implements Initializable {

    @FXML private TableView<Activite> tableActivite;
    @FXML private TableColumn<Activite, String>  colNom;
    @FXML private TableColumn<Activite, String>  colDescription;
    @FXML private TableColumn<Activite, String>  colLieu;
    @FXML private TableColumn<Activite, Integer> colDuree;
    @FXML private TableColumn<Activite, Double>  colPrix;
    @FXML private TableColumn<Activite, Integer> colCapacite;
    @FXML private TableColumn<Activite, String>  colCategorie;
    @FXML private TableColumn<Activite, Void>    colActions;
    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;

    private ActiviteService as = new ActiviteService();
    private ObservableList<Activite> activiteList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        // Actions column: Edit and Delete buttons
        colActions.setCellFactory(column -> new TableCell<Activite, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("btn-action", "btn-warning");
                btnDelete.getStyleClass().addAll("btn-action", "btn-danger");
                container.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(event -> {
                    Activite a = getTableView().getItems().get(getIndex());
                    openModal(a);
                });

                btnDelete.setOnAction(event -> {
                    Activite a = getTableView().getItems().get(getIndex());
                    handleDeleteSpecific(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        // Catégorie badge
        colCategorie.setCellFactory(col -> new TableCell<Activite, String>() {
            @Override
            protected void updateItem(String categorie, boolean empty) {
                super.updateItem(categorie, empty);
                if (empty || categorie == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(categorie);
                    badge.getStyleClass().add("badge-default");
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Prix price tag
        colPrix.setCellFactory(col -> new TableCell<Activite, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label tag = new Label(String.format("💰 %.0f DT", prix));
                    tag.getStyleClass().add("price-tag");
                    HBox box = new HBox(tag);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Durée display
        colDuree.setCellFactory(col -> new TableCell<Activite, Integer>() {
            @Override
            protected void updateItem(Integer duree, boolean empty) {
                super.updateItem(duree, empty);
                if (empty || duree == null) {
                    setText(null);
                } else {
                    setText("⏱ " + duree + " min");
                }
            }
        });

        loadData();
    }

    public void loadData() {
        try {
            activiteList = FXCollections.observableArrayList(as.recupToutesActivites());

            if (lblTotal != null) {
                lblTotal.setText(String.valueOf(activiteList.size()));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement données activité: " + e.getMessage());
            activiteList = FXCollections.observableArrayList();
        }

        FilteredList<Activite> filteredData = new FilteredList<>(activiteList, p -> true);

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(activite -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                boolean matchesNom = activite.getNom() != null && activite.getNom().toLowerCase().contains(lowerCaseFilter);
                boolean matchesLieu = activite.getLieu() != null && activite.getLieu().toLowerCase().contains(lowerCaseFilter);

                return matchesNom || matchesLieu;
            });
            if (lblTotal != null) {
                lblTotal.setText(String.valueOf(filteredData.size()));
            }
        });

        tableActivite.setItems(filteredData);
    }

    @FXML
    private void handleSortByCategorie() {
        if (activiteList != null) {
            FXCollections.sort(activiteList, (a1, a2) -> {
                String c1 = a1.getCategorie() != null ? a1.getCategorie() : "";
                String c2 = a2.getCategorie() != null ? a2.getCategorie() : "";
                return c1.compareToIgnoreCase(c2);
            });
        }
    }

    @FXML
    private void handleSortByLieu() {
        if (activiteList != null) {
            FXCollections.sort(activiteList, (a1, a2) -> {
                String l1 = a1.getLieu() != null ? a1.getLieu() : "";
                String l2 = a2.getLieu() != null ? a2.getLieu() : "";
                return l1.compareToIgnoreCase(l2);
            });
        }
    }

    @FXML
    private void handleAdd() {
        openModal(null);
    }

    private void handleDeleteSpecific(Activite selected) {
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette activité ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    as.supprimerActivite(selected);
                    loadData();
                } catch (SQLException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur de suppression");
                    errorAlert.setContentText("Impossible de supprimer l'activité: " + e.getMessage());
                    errorAlert.show();
                }
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleExportPdf() {
        String[] headers = {"ID", "Nom", "Description", "Lieu", "Durée (min)", "Prix", "Capacité Max", "Catégorie"};
        Function<Activite, String>[] extractors = new Function[]{
            (Function<Activite, String>) a -> String.valueOf(a.getIdActivite()),
            (Function<Activite, String>) a -> a.getNom(),
            (Function<Activite, String>) a -> a.getDescription(),
            (Function<Activite, String>) a -> a.getLieu(),
            (Function<Activite, String>) a -> String.valueOf(a.getDuree()),
            (Function<Activite, String>) a -> String.valueOf(a.getPrix()) + " DT",
            (Function<Activite, String>) a -> String.valueOf(a.getCapaciteMax()),
            (Function<Activite, String>) a -> a.getCategorie()
        };

        PdfExporter.exportToPdf(
            "Liste des Activités Touristiques",
            "Activites_List.pdf",
            tableActivite.getItems(),
            headers,
            extractors
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText(null);
        alert.setContentText("Le fichier PDF a été généré sur votre Bureau : Activites_List.pdf");
        alert.show();
    }

    private void openModal(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ActiviteForm.fxml"));
            Parent root = loader.load();

            ActiviteFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setActivite(activite);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(activite == null ? "Ajouter une Activité" : "Modifier l'Activité");

            Scene scene = new Scene(root);
            if (tableActivite.getScene() != null && !tableActivite.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableActivite.getScene().getStylesheets().get(0));
            } else {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur chargement modal: " + e.getMessage());
        }
    }
}
