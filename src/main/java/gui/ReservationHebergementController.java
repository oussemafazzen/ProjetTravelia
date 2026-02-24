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
import models.ReservationHebergement;
import services.ReservationHebergementService;
import utils.PdfExporter;
import java.util.function.Function;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

public class ReservationHebergementController implements Initializable {

    @FXML private TableView<ReservationHebergement> tableReservation;
    @FXML private TableColumn<ReservationHebergement, Void> colActions;
    @FXML private TableColumn<ReservationHebergement, Date>    colDateDebut;
    @FXML private TableColumn<ReservationHebergement, Date>    colDateFin;
    @FXML private TableColumn<ReservationHebergement, Integer> colNbPersonnes;
    @FXML private TableColumn<ReservationHebergement, String>  colStatut;
    @FXML private TableColumn<ReservationHebergement, Integer> colIdClient;
    @FXML private TableColumn<ReservationHebergement, String>  colIdHebergement;

    @FXML private Label lblTotal;
    @FXML private Label lblConfirmed;
    @FXML private Label lblPending;

    private ReservationHebergementService rhs = new ReservationHebergementService();
    private ObservableList<ReservationHebergement> reservationList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colNbPersonnes.setCellValueFactory(new PropertyValueFactory<>("nombrePersonnes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colIdClient.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colIdHebergement.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHebergement() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHebergement().getNom());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // ── Custom Cell Factories ──────────────────────────────────────────

        // Actions column: Edit and Delete buttons
        colActions.setCellFactory(column -> new TableCell<ReservationHebergement, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("btn-action", "btn-warning");
                btnDelete.getStyleClass().addAll("btn-action", "btn-danger");
                container.setAlignment(Pos.CENTER);
                
                btnEdit.setOnAction(event -> {
                    ReservationHebergement r = getTableView().getItems().get(getIndex());
                    openModal(r);
                });
                
                btnDelete.setOnAction(event -> {
                    ReservationHebergement r = getTableView().getItems().get(getIndex());
                    handleDeleteSpecific(r);
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

        // Status badge (confirmed=green, pending=yellow, cancelled=red)
        colStatut.setCellFactory(col -> new TableCell<ReservationHebergement, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    String lower = statut.toLowerCase();
                    if (lower.contains("confirm") || lower.equals("confirmed") || lower.equals("confirmée") || lower.equals("confirmé")) {
                        badge.setText("✅  " + statut);
                        badge.getStyleClass().add("badge-confirmed");
                    } else if (lower.contains("attente") || lower.contains("pending") || lower.contains("en cours")) {
                        badge.setText("⏳  " + statut);
                        badge.getStyleClass().add("badge-pending");
                    } else if (lower.contains("annul") || lower.contains("cancel")) {
                        badge.setText("❌  " + statut);
                        badge.getStyleClass().add("badge-cancelled");
                    } else {
                        badge.setText(statut);
                        badge.getStyleClass().add("badge-default");
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Person count tag
        colNbPersonnes.setCellFactory(col -> new TableCell<ReservationHebergement, Integer>() {
            @Override
            protected void updateItem(Integer nb, boolean empty) {
                super.updateItem(nb, empty);
                if (empty || nb == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label tag = new Label("👥 " + nb);
                    tag.getStyleClass().add("persons-tag");
                    HBox box = new HBox(tag);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        loadData();
    }

    public void loadData() {
        reservationList = FXCollections.observableArrayList(rhs.getAll());
        tableReservation.setItems(reservationList);

        // Update stats labels
        if (lblTotal != null) lblTotal.setText(String.valueOf(reservationList.size()));
        if (lblConfirmed != null) {
            long confirmed = reservationList.stream()
                .filter(r -> r.getStatut() != null && (r.getStatut().toLowerCase().contains("confirm")))
                .count();
            lblConfirmed.setText(String.valueOf(confirmed));
        }
        if (lblPending != null) {
            long pending = reservationList.stream()
                .filter(r -> r.getStatut() != null && (r.getStatut().toLowerCase().contains("attente") || r.getStatut().toLowerCase().contains("pending") || r.getStatut().toLowerCase().contains("en cours")))
                .count();
            lblPending.setText(String.valueOf(pending));
        }
    }

    @FXML
    private void handleAdd() {
        openModal(null);
    }

    @FXML
    private void handleDeleteSpecific(ReservationHebergement selected) {
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                rhs.delete(selected);
                loadData();
            }
        }
    }

    @FXML
    private void handleStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ReservationStatsView.fxml"));
            Parent root = loader.load();

            ReservationStatsController controller = loader.getController();
            controller.setData(reservationList);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Statistiques Saisonnières");

            Scene scene = new Scene(root);
            if (tableReservation.getScene() != null && !tableReservation.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableReservation.getScene().getStylesheets().get(0));
            } else {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur chargement stats: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPdf() {
        String[] headers = {"ID", "Date Début", "Date Fin", "Personnes", "Statut", "ID Client"};
        Function<ReservationHebergement, String>[] extractors = new Function[]{
            (Function<ReservationHebergement, String>) r -> String.valueOf(r.getIdReservationHebergement()),
            (Function<ReservationHebergement, String>) r -> r.getDateDebut().toString(),
            (Function<ReservationHebergement, String>) r -> r.getDateFin().toString(),
            (Function<ReservationHebergement, String>) r -> String.valueOf(r.getNombrePersonnes()),
            (Function<ReservationHebergement, String>) r -> r.getStatut(),
            (Function<ReservationHebergement, String>) r -> String.valueOf(r.getIdClient())
        };

        PdfExporter.exportToPdf(
            "Liste des Réservations",
            "Reservations_List.pdf",
            tableReservation.getItems(),
            headers,
            extractors
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText(null);
        alert.setContentText("Le fichier PDF a été généré sur votre Bureau : Reservations_List.pdf");
        alert.show();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void openModal(ReservationHebergement reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ReservationHebergementForm.fxml"));
            Parent root = loader.load();

            ReservationHebergementFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setReservation(reservation);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(reservation == null ? "Nouvelle Réservation" : "Modifier la Réservation");

            Scene scene = new Scene(root);
            if (tableReservation.getScene() != null && !tableReservation.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().add(tableReservation.getScene().getStylesheets().get(0));
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
