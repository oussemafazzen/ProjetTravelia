package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML
    private TableView<ReservationHebergement> tableReservation;
    @FXML
    private TableColumn<ReservationHebergement, Integer> colId;
    @FXML
    private TableColumn<ReservationHebergement, Date> colDateDebut;
    @FXML
    private TableColumn<ReservationHebergement, Date> colDateFin;
    @FXML
    private TableColumn<ReservationHebergement, Integer> colNbPersonnes;
    @FXML
    private TableColumn<ReservationHebergement, String> colStatut;
    @FXML
    private TableColumn<ReservationHebergement, Integer> colIdClient;
    @FXML
    private TableColumn<ReservationHebergement, String> colIdHebergement;

    private ReservationHebergementService rhs = new ReservationHebergementService();
    private ObservableList<ReservationHebergement> reservationList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReservationHebergement"));
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

        loadData();
    }

    public void loadData() {
        reservationList = FXCollections.observableArrayList(rhs.getAll());
        tableReservation.setItems(reservationList);
    }

    @FXML
    private void handleAdd() {
        openModal(null);
    }

    @FXML
    private void handleUpdate() {
        ReservationHebergement selected = tableReservation.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openModal(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réservation à modifier.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        ReservationHebergement selected = tableReservation.getSelectionModel().getSelectedItem();
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
            // Apply current theme from main window
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
            // Apply current theme from main window
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
