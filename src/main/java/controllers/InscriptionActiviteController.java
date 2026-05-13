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
import models.InscriptionActivite;
import services.InscriptionActiviteService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Function;
import utils.PdfExporter;

public class InscriptionActiviteController implements Initializable {

    @FXML private TableView<InscriptionActivite> tableInscription;
    @FXML private TableColumn<InscriptionActivite, Date>    colDateActivite;
    @FXML private TableColumn<InscriptionActivite, Integer> colNombreParticipants;
    @FXML private TableColumn<InscriptionActivite, String>  colStatut;
    @FXML private TableColumn<InscriptionActivite, Integer> colIdClient;
    @FXML private TableColumn<InscriptionActivite, Integer> colIdActivite;
    @FXML private TableColumn<InscriptionActivite, Void>    colActions;
    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;

    private InscriptionActiviteService is = new InscriptionActiviteService();
    private ObservableList<InscriptionActivite> inscriptionList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colDateActivite.setCellValueFactory(new PropertyValueFactory<>("dateActivite"));
        colNombreParticipants.setCellValueFactory(new PropertyValueFactory<>("nombreParticipants"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colIdClient.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colIdActivite.setCellValueFactory(new PropertyValueFactory<>("idActivite"));

        // Actions column
        colActions.setCellFactory(column -> new TableCell<InscriptionActivite, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("btn-action", "btn-warning");
                btnDelete.getStyleClass().addAll("btn-action", "btn-danger");
                container.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(event -> {
                    InscriptionActivite i = getTableView().getItems().get(getIndex());
                    openModal(i);
                });

                btnDelete.setOnAction(event -> {
                    InscriptionActivite i = getTableView().getItems().get(getIndex());
                    handleDeleteSpecific(i);
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

        // Statut badge
        colStatut.setCellFactory(col -> new TableCell<InscriptionActivite, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(statut);
                    String lower = statut.toLowerCase();
                    if (lower.contains("confirm")) {
                        badge.getStyleClass().add("badge-hotel");
                    } else if (lower.contains("annul")) {
                        badge.getStyleClass().add("badge-auberge");
                    } else {
                        badge.getStyleClass().add("badge-default");
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        loadData();
    }

    public void loadData() {
        try {
            inscriptionList = FXCollections.observableArrayList(is.recupToutesInscriptions());

            if (lblTotal != null) {
                lblTotal.setText(String.valueOf(inscriptionList.size()));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement données inscription: " + e.getMessage());
            inscriptionList = FXCollections.observableArrayList();
        }

        FilteredList<InscriptionActivite> filteredData = new FilteredList<>(inscriptionList, p -> true);

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(inscription -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                boolean matchesStatut = inscription.getStatut() != null && inscription.getStatut().toLowerCase().contains(lowerCaseFilter);
                boolean matchesId = String.valueOf(inscription.getIdActivite()).contains(lowerCaseFilter);

                return matchesStatut || matchesId;
            });
            if (lblTotal != null) {
                lblTotal.setText(String.valueOf(filteredData.size()));
            }
        });

        tableInscription.setItems(filteredData);
    }

    @FXML
    private void handleSortByDate() {
        if (inscriptionList != null) {
            FXCollections.sort(inscriptionList, (i1, i2) -> {
                Date d1 = i1.getDateActivite();
                Date d2 = i2.getDateActivite();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            });
        }
    }

    @FXML
    private void handleSortByStatut() {
        if (inscriptionList != null) {
            FXCollections.sort(inscriptionList, (i1, i2) -> {
                String s1 = i1.getStatut() != null ? i1.getStatut() : "";
                String s2 = i2.getStatut() != null ? i2.getStatut() : "";
                return s1.compareToIgnoreCase(s2);
            });
        }
    }

    @FXML
    private void handleAdd() {
        openModal(null);
    }

    private void handleDeleteSpecific(InscriptionActivite selected) {
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette inscription ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    is.supprimerInscription(selected);
                    loadData();
                } catch (SQLException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur de suppression");
                    errorAlert.setContentText("Impossible de supprimer l'inscription: " + e.getMessage());
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
        String[] headers = {"Date Activité", "Participants", "Statut", "ID Client", "ID Activité"};
        Function<InscriptionActivite, String>[] extractors = new Function[]{
            (Function<InscriptionActivite, String>) i -> i.getDateActivite() != null ? i.getDateActivite().toString() : "",
            (Function<InscriptionActivite, String>) i -> String.valueOf(i.getNombreParticipants()),
            (Function<InscriptionActivite, String>) i -> i.getStatut(),
            (Function<InscriptionActivite, String>) i -> String.valueOf(i.getIdClient()),
            (Function<InscriptionActivite, String>) i -> String.valueOf(i.getIdActivite())
        };

        PdfExporter.exportToPdf(
            "Liste des Inscriptions aux Activités",
            "Inscriptions_Activites_List.pdf",
            tableInscription.getItems(),
            headers,
            extractors
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText(null);
        alert.setContentText("Le fichier PDF a été généré sur votre Bureau : Inscriptions_Activites_List.pdf");
        alert.show();
    }

    private void openModal(InscriptionActivite inscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InscriptionActiviteForm.fxml"));
            Parent root = loader.load();

            InscriptionActiviteFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setInscription(inscription);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(inscription == null ? "Ajouter une Inscription" : "Modifier l'Inscription");

            Scene scene = new Scene(root);
            if (tableInscription.getScene() != null && !tableInscription.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableInscription.getScene().getStylesheets().get(0));
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
