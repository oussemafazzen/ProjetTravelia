package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.models.Billet;
import org.example.models.Reservation;
import org.example.services.ServiceBillet;
import org.example.services.ServiceReservation;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.services.ServiceClient;

public class ReservationsController {

    @FXML private Button btnHome;
    @FXML private Button btnClients;
    @FXML private Button btnReservations;
    @FXML private Button btnHebergement;
    @FXML private Button btnActivites;
    @FXML private Button btnAvis;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblTotalBillets;
    @FXML private Label lblMontantTotal;
    @FXML private VBox reservationsList;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ServiceClient sc = new ServiceClient();

    @FXML
    public void initialize() {
        setActive(btnReservations);
        loadStats();
        loadReservations();
    }

    /* ---------------- NAV ACTIVE ---------------- */

    private void setActive(Button active) {
        Button[] all = {btnHome, btnClients, btnReservations, btnHebergement, btnActivites, btnAvis};
        for (Button b : all) {
            if (b != null) b.getStyleClass().remove("nav-item-active");
        }
        if (active != null && !active.getStyleClass().contains("nav-item-active")) {
            active.getStyleClass().add("nav-item-active");
        }
    }

    /* ---------------- NAV ACTIONS ---------------- */

    @FXML private void goHome(ActionEvent e) {
        setActive(btnHome);
        switchScene(e, "/fxml/test.fxml");
    }

    @FXML private void goClients(ActionEvent e) {
        setActive(btnClients);
        openIfExistsOrWarn(e, "/fxml/clients.fxml", "Clients", btnReservations);
    }

    @FXML private void goReservations(ActionEvent e) {
        setActive(btnReservations);
        // déjà sur la page
    }

    @FXML private void goHebergement(ActionEvent e) {
        setActive(btnHebergement);
        openIfExistsOrWarn(e, "/fxml/hebergement.fxml", "Hébergement", btnReservations);
    }

    @FXML private void goActivites(ActionEvent e) {
        setActive(btnActivites);
        openIfExistsOrWarn(e, "/fxml/activites.fxml", "Activités", btnReservations);
    }

    @FXML private void goAvis(ActionEvent e) {
        setActive(btnAvis);
        openIfExistsOrWarn(e, "/fxml/avis.fxml", "Avis", btnReservations);
    }

    private void openIfExistsOrWarn(ActionEvent e, String fxmlPath, String moduleName, Button fallbackActive) {
        if (getClass().getResource(fxmlPath) == null) {
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Module \"" + moduleName + "\" pas encore disponible.\nCrée " + fxmlPath + " pour l'activer."
            ).showAndWait();
            setActive(fallbackActive);
            return;
        }
        switchScene(e, fxmlPath);
    }

    private void switchScene(ActionEvent e, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            stage.setScene(scene);
            stage.setWidth(1300);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.toString()).showAndWait();
        }
    }

    /* ---------------- PAGE CONTENT ---------------- */

    private void loadStats() {
        int totalReservations = sr.getTotalReservations();
        int totalBillets = sb.getTotalBillets();
        double montantTotal = sb.getMontantTotal();

        lblTotalReservations.setText(String.valueOf(totalReservations));
        lblTotalBillets.setText(String.valueOf(totalBillets));
        lblMontantTotal.setText(fmtDT(montantTotal));
    }

    private void loadReservations() {
        if (reservationsList == null) return;

        reservationsList.getChildren().clear();

        List<Reservation> reservations = sr.getAll();
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets));
        }
    }

    /* ========== NEW CARD LAYOUT (comme la 2ème photo) ========== */

    private VBox buildReservationCard(Reservation r, List<Billet> billets) {
        VBox card = new VBox(14);
        card.getStyleClass().add("big-card");

        // HEADER
        HBox header = new HBox(10);
        Label title = new Label("Réservation #" + r.getIdReservation());
        title.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(normalizeStatusText(safe(r.getStatut())));
        status.getStyleClass().addAll("status-badge", statusClass(safe(r.getStatut())));

        header.getChildren().addAll(title, spacer, status);

        // META ROW (date + paiement)
        HBox metaRow = new HBox(18);
        metaRow.getStyleClass().add("meta-row");
        metaRow.setAlignment(Pos.CENTER_LEFT);

        String dateTxt = (r.getDateReservation() != null) ? r.getDateReservation().format(df) : "";
        String payTxt = safe(r.getModalitesPaiement());

        metaRow.getChildren().addAll(
                metaItem("🕒", dateTxt),
                metaItem("💳", payTxt)
        );

        // CLIENT BOX (violet) - on tente de récupérer un nom client si dispo
        VBox clientBox = new VBox(4);
        clientBox.getStyleClass().add("client-box");

        Label clientLbl = new Label("Client");
        clientLbl.getStyleClass().add("muted-small");

        String clientName = findClientName(r);
        if (clientName.isBlank()) clientName = "Client";
        Label clientNameLbl = new Label(clientName);
        clientNameLbl.getStyleClass().add("client-name");

        clientBox.getChildren().addAll(clientLbl, clientNameLbl);

        // Billets title
        int n = (billets == null) ? 0 : billets.size();
        Label billetsTitle = new Label("Billets (" + n + ")");
        billetsTitle.getStyleClass().add("section-mini");

        // Liste billets
        VBox ticketsBox = new VBox(14);
        if (billets != null) {
            for (Billet b : billets) {
                ticketsBox.getChildren().add(buildTicketRow(b));
            }
        }

        // Separator soft
        Separator sep = new Separator();
        sep.getStyleClass().add("soft-sep");

        // TOTAL
        double total = 0;
        if (billets != null) {
            for (Billet b : billets) total += b.getPrix();
        }

        HBox totalRow = new HBox(10);
        totalRow.getStyleClass().add("total-row");
        totalRow.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label("Total de la réservation");
        totalLabel.getStyleClass().add("total-label");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label totalAmount = new Label(fmtDT(total));
        totalAmount.getStyleClass().add("total-amount");

        totalRow.getChildren().addAll(totalLabel, spacer2, totalAmount);

        // ACTIONS
        HBox actions = new HBox(10);
        actions.getStyleClass().add("actions-row");
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-outline");

        Button btnPrint = new Button("Imprimer");
        btnPrint.getStyleClass().add("btn-outline");

        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("btn-danger");

        actions.getChildren().addAll(btnEdit, btnPrint, btnCancel);

        card.getChildren().addAll(header, metaRow, clientBox, billetsTitle, ticketsBox, sep, totalRow, actions);
        return card;
    }

    private HBox metaItem(String icon, String text) {
        HBox box = new HBox(8);
        box.getStyleClass().add("meta-item");
        box.setAlignment(Pos.CENTER_LEFT);

        Label ic = new Label(icon);
        ic.getStyleClass().add("meta-icon");

        Label t = new Label(text);
        t.getStyleClass().add("muted");

        box.getChildren().addAll(ic, t);
        return box;
    }

    private HBox buildTicketRow(Billet b) {
        HBox row = new HBox(16);
        row.getStyleClass().add("ticket-row");
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane icon = new StackPane();
        icon.getStyleClass().add("ticket-icon");

        Label ic = new Label(getIconForTransport(b.getTypeTransport()));
        ic.getStyleClass().add("ticket-icon-text");
        icon.getChildren().add(ic);

        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);

        // Titre: si tu as des villes dans Billet un jour, on pourra mettre "Paris → Tokyo"
        Label title = new Label(safe(b.getNumeroBillet()));
        title.getStyleClass().add("ticket-title");

        Label meta1 = new Label("Transport: " + safe(b.getTypeTransport()));
        meta1.getStyleClass().add("muted-small");

        String depart = b.getDateDepart() != null ? b.getDateDepart().format(df) : "";
        String arrivee = b.getDateArrivee() != null ? b.getDateArrivee().format(df) : "";
        Label meta2 = new Label("Départ: " + depart + " → Arrivée: " + arrivee);
        meta2.getStyleClass().add("muted-small");

        left.getChildren().addAll(title, meta1, meta2);

        VBox right = new VBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label price = new Label(fmtDT(b.getPrix()));
        price.getStyleClass().add("ticket-price");

        Label badge = new Label(normalizeStatusText(safe(b.getStatut())));
        badge.getStyleClass().addAll("status-badge", statusClass(safe(b.getStatut())));

        right.getChildren().addAll(price, badge);

        row.getChildren().addAll(icon, left, right);
        return row;
    }

    /* ---------------- Helpers ---------------- */

    private String fmtDT(double amount) {
        return String.format("%.0f DT", amount);
    }

    private String getIconForTransport(String type) {
        if (type == null) return "🎫";
        return switch (type.toLowerCase()) {
            case "avion" -> "✈";
            case "train" -> "🚆";
            case "bus" -> "🚌";
            default -> "🎫";
        };
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // Map statut -> class (confirmed/pending/cancelled)
    private String statusClass(String raw) {
        String s = safe(raw).toLowerCase();
        if (s.contains("confirm")) return "status-confirmed";
        if (s.contains("attente") || s.contains("en_attente") || s.contains("pending")) return "status-pending";
        if (s.contains("annul") || s.contains("cancel")) return "status-cancelled";
        return "status-pending";
    }

    private String normalizeStatusText(String raw) {
        String s = safe(raw).trim();
        if (s.isEmpty()) return "en_attente";
        return s.replace('_', ' ');
    }

    private String findClientName(Reservation r) {

        // A) si Reservation contient déjà un nom client, on prend
        String direct = tryGetString(r, "getClientName", "getNomClient", "getClientNom", "getClientFullName");
        if (!direct.isBlank() && !"Client".equalsIgnoreCase(direct)) return direct;

        // B) sinon on récupère l'id client depuis Reservation
        Integer clientId = tryGetInt(r, "getIdClient", "getClientId", "getId_client", "getClient_id");
        if (clientId != null && clientId > 0) {
            return sc.getFullNameById(clientId);
        }

        return "Client";
    }

    private String tryGetString(Object obj, String... methods) {
        for (String m : methods) {
            try {
                var method = obj.getClass().getMethod(m);
                Object val = method.invoke(obj);
                if (val != null) {
                    String s = val.toString().trim();
                    if (!s.isEmpty()) return s;
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    private Integer tryGetInt(Object obj, String... methods) {
        for (String m : methods) {
            try {
                var method = obj.getClass().getMethod(m);
                Object val = method.invoke(obj);
                if (val == null) continue;
                if (val instanceof Number n) return n.intValue();
                String s = val.toString().trim();
                if (!s.isEmpty()) return Integer.parseInt(s);
            } catch (Exception ignored) {}
        }
        return null;
    }
}