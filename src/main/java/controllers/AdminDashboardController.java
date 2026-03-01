package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import models.ReservationAdminRow;
import services.ServiceReservation;
import utils.ExcelExportUtil;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private TextField txtSearch;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblTotalBillets;
    @FXML private Label lblMontantTotal;

    @FXML private TableView<ReservationAdminRow> table;

    @FXML private TableColumn<ReservationAdminRow, String> colPays;
    @FXML private TableColumn<ReservationAdminRow, String> colClient;
    @FXML private TableColumn<ReservationAdminRow, String> colDate;
    @FXML private TableColumn<ReservationAdminRow, String> colStatut;
    @FXML private TableColumn<ReservationAdminRow, String> colPaiement;
    @FXML private TableColumn<ReservationAdminRow, Void> colActions;

    private final ServiceReservation sr = new ServiceReservation();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        refreshAll();
    }

    private void setupColumns() {
        if (colPays != null) colPays.setCellValueFactory(new PropertyValueFactory<>("paysdestination"));

        if (colClient != null) {
            colClient.setCellValueFactory(cell -> {
                ReservationAdminRow r = cell.getValue();
                String full = (r == null) ? "" : r.getClientFullName();
                return new javafx.beans.property.SimpleStringProperty(full);
            });
        }

        if (colDate != null) {
            colDate.setCellValueFactory(cell -> {
                ReservationAdminRow r = cell.getValue();
                String date = (r != null && r.getDateReservation() != null) ? r.getDateReservation().format(df) : "-";
                return new javafx.beans.property.SimpleStringProperty(date);
            });
        }

        if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colPaiement != null) colPaiement.setCellValueFactory(new PropertyValueFactory<>("modalitesPaiement"));

        if (colActions != null) {
            colActions.setCellFactory(tc -> new TableCell<>() {

                private final Button btnEdit = new Button("Modifier");
                private final Button btnDelete = new Button("Supprimer");
                private final Button btnBlock = new Button("Bloc/Débloq");

                private final HBox box = new HBox(8, btnEdit, btnDelete, btnBlock);

                {
                    box.setAlignment(Pos.CENTER_LEFT);
                    btnEdit.getStyleClass().add("btn-outline");
                    btnDelete.getStyleClass().add("btn-danger");
                    btnBlock.getStyleClass().add("btn-secondary");

                    btnEdit.setOnAction(e -> onEditRow(getRow()));
                    btnDelete.setOnAction(e -> onDeleteRow(getRow()));
                    btnBlock.setOnAction(e -> onToggleBlockRow(getRow()));
                }

                private ReservationAdminRow getRow() {
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return null;
                    return getTableView().getItems().get(getIndex());
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
        }
    }

    // =================== EVENTS ===================

    @FXML
    private void onSearch(ActionEvent e) {
        String q = (txtSearch == null) ? "" : txtSearch.getText();
        q = (q == null) ? "" : q.trim();

        List<ReservationAdminRow> rows = (q.isEmpty())
                ? sr.getAllAdminRows()
                : sr.searchAdminRows(q);

        if (table != null) table.setItems(FXCollections.observableArrayList(rows));
        updateStats();
    }

    @FXML
    private void onRefresh(ActionEvent e) {
        refreshAll();
    }

    @FXML
    private void onExport(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter les réservations en Excel");
        fc.setInitialFileName("reservations.xlsx");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx"));

        File file = fc.showSaveDialog(table != null ? table.getScene().getWindow() : null);
        if (file == null) return;

        try {
            ExcelExportUtil.exportAdminReservations(sr, file.getAbsolutePath());
            new Alert(Alert.AlertType.INFORMATION,
                    "Export réussi !\n" + file.getAbsolutePath(), ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de l'export : " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // ===== Sidebar navigation =====
    @FXML private void goUsers(ActionEvent e) { info("Navigation", "Gestion Utilisateurs (à connecter)."); }
    @FXML private void goAdmins(ActionEvent e) { info("Navigation", "Gestion Administrateurs (à connecter)."); }
    @FXML private void goReservations(ActionEvent e) { /* déjà */ }
    @FXML private void goHebergement(ActionEvent e) { info("Navigation", "Gestion Hébergements (à connecter)."); }
    @FXML private void goActivites(ActionEvent e) { info("Navigation", "Gestion Activités (à connecter)."); }
    @FXML private void goAvis(ActionEvent e) { info("Navigation", "Gestion Avis (à connecter)."); }
    @FXML private void goStats(ActionEvent e) { info("Navigation", "Statistiques (à connecter)."); }
    @FXML private void onLogout(ActionEvent e) { info("Session", "Déconnexion (à connecter)."); }

    // =================== CORE ===================

    private void refreshAll() {
        List<ReservationAdminRow> rows = sr.getAllAdminRows();
        if (table != null) table.setItems(FXCollections.observableArrayList(rows));
        updateStats();
    }

    private void updateStats() {
        int totalRes = sr.countAll();
        int totalBil = sr.countBilletsAll();
        double totalAmount = sr.sumBilletsAll();

        if (lblTotalReservations != null) lblTotalReservations.setText(String.valueOf(totalRes));
        if (lblTotalBillets != null) lblTotalBillets.setText(String.valueOf(totalBil));
        if (lblMontantTotal != null) lblMontantTotal.setText(String.format("%.0f DT", totalAmount));
    }

    // =================== ACTIONS HELPERS ===================

    private void onEditRow(ReservationAdminRow row) {
        if (row == null) return;
        info("Modifier", "Modifier réservation #" + row.getIdReservation() + " (à implémenter).");
    }

    private void onDeleteRow(ReservationAdminRow row) {
        if (row == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la réservation #" + row.getIdReservation() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            info("Supprimer", "Suppression (à implémenter) pour #" + row.getIdReservation());
            refreshAll();
        }
    }

    private void onToggleBlockRow(ReservationAdminRow row) {
        if (row == null) return;
        info("Bloc/Débloq", "Bloc/Débloq (à implémenter) pour #" + row.getIdReservation());
        refreshAll();
    }

    // =================== UI HELPERS ===================

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}