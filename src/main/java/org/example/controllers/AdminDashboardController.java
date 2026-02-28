package org.example.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.ReservationAdminRow;
import org.example.services.ServiceReservation;
import org.example.utils.ExcelExportXlsxUtil;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private TextField txtSearch;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblTotalBillets;
    @FXML private Label lblMontantTotal;

    @FXML private TableView<ReservationAdminRow> table;
    @FXML private TableColumn<ReservationAdminRow, Number> colId;
    @FXML private TableColumn<ReservationAdminRow, String> colClient;
    @FXML private TableColumn<ReservationAdminRow, String> colDate;
    @FXML private TableColumn<ReservationAdminRow, String> colStatut;
    @FXML private TableColumn<ReservationAdminRow, String> colPaiement;
    @FXML private TableColumn<ReservationAdminRow, ReservationAdminRow> colActions;

    private final ServiceReservation sr = new ServiceReservation();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTable();
        loadAll();
    }

    private void setupTable() {

        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getIdReservation()));
        colClient.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(safe(c.getValue().getClientFullName())));
        colDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(safe(c.getValue().getDateReservation())));
        colStatut.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(safe(c.getValue().getStatut())));
        colPaiement.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(safe(c.getValue().getModalitesPaiement())));

        // actions buttons
        colActions.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colActions.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(ReservationAdminRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                Button btnEdit = new Button("Modifier");
                Button btnDelete = new Button("Supprimer");
                Button btnBlock = new Button("Bloc/Débloq");

                btnEdit.getStyleClass().add("btn-primary");
                btnDelete.getStyleClass().add("btn-secondary");
                btnBlock.getStyleClass().add("btn-outline");

                btnEdit.setOnAction(e -> onEditRow(row));
                btnDelete.setOnAction(e -> onDeleteRow(row));
                btnBlock.setOnAction(e -> onToggleBlockRow(row));

                HBox box = new HBox(8, btnEdit, btnDelete, btnBlock);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    // ===================== LOAD =====================

    private void loadAll() {
        try {
            // KPI
            int totalRes = sr.countAll();
            int totalBil = sr.countBilletsAll();
            double totalAmount = sr.sumBilletsAll();

            lblTotalReservations.setText(String.valueOf(totalRes));
            lblTotalBillets.setText(String.valueOf(totalBil));
            lblMontantTotal.setText(String.format("%.0f DT", totalAmount));

            // Table
            List<ReservationAdminRow> rows = sr.getAllAdminRows(null);
            table.setItems(FXCollections.observableArrayList(rows));

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur loadAll: " + ex.getMessage());
        }
    }

    @FXML
    private void onRefresh(ActionEvent e) {
        txtSearch.setText("");
        loadAll();
    }

    @FXML
    private void onSearch(ActionEvent e) {
        try {
            String q = txtSearch.getText();
            List<ReservationAdminRow> rows = sr.getAllAdminRows(q);
            table.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur recherche: " + ex.getMessage());
        }
    }

    // ===================== EXPORT EXCEL (REEL) =====================
    @FXML
    private void onExport(ActionEvent e) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exporter Excel");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            fc.setInitialFileName("reservations.xlsx");

            Stage st = (Stage) ((Node) e.getSource()).getScene().getWindow();
            File file = fc.showSaveDialog(st);
            if (file == null) return;

            // ✅ Export (réel)
            ExcelExportXlsxUtil.exportAdminReservations(sr, file.getAbsolutePath());

            alert(Alert.AlertType.INFORMATION, "Export terminé ✅\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Export échoué: " + ex.getMessage());
        }
    }

    // ===================== ACTIONS ROWS (placeholders safe) =====================

    private void onEditRow(ReservationAdminRow row) {
        alert(Alert.AlertType.INFORMATION, "Modifier réservation #" + row.getIdReservation() + " (popup à brancher).");
    }

    private void onDeleteRow(ReservationAdminRow row) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer réservation #" + row.getIdReservation() + " ?",
                ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    sr.delete(row.getIdReservation());
                    loadAll();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Erreur suppression: " + ex.getMessage());
                }
            }
        });
    }

    private void onToggleBlockRow(ReservationAdminRow row) {
        // si tu as un champ "blocked" côté user/client -> brancher ici
        alert(Alert.AlertType.INFORMATION, "Bloc/Débloq (à brancher selon ta table user/client).");
    }

    // ===================== NAV (les méthodes doivent exister sinon bouton mort) =====================

    @FXML private void goUsers(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Gestion Utilisateurs (à brancher)."); }
    @FXML private void goAdmins(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Gestion Administrateurs (à brancher)."); }
    @FXML private void goReservations(ActionEvent e) { /* déjà sur la page */ }
    @FXML private void goHebergement(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Gestion Hébergements (à brancher)."); }
    @FXML private void goActivites(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Gestion Activités (à brancher)."); }
    @FXML private void goAvis(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Gestion Avis (à brancher)."); }

    @FXML
    private void goStats(ActionEvent e) {
        switchScene(e, "/fxml/stats.fxml");
    }

    @FXML
    private void onLogout(ActionEvent e) {
        alert(Alert.AlertType.INFORMATION, "Déconnexion (à brancher).");
    }

    private void switchScene(ActionEvent e, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/admin.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur ouverture: " + fxmlPath + "\n" + ex.getMessage());
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private void alert(Alert.AlertType t, String msg) {
        new Alert(t, msg, ButtonType.OK).showAndWait();
    }
}