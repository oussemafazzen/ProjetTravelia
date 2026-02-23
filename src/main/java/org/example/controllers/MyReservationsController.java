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
import org.example.utils.SessionContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyReservationsController {

    @FXML private Button btnHome;
    @FXML private Button btnClients;
    @FXML private Button btnReservations; // Mes Réservations (client)
    @FXML private Button btnHebergement;
    @FXML private Button btnActivites;
    @FXML private Button btnAvis;

    @FXML private VBox reservationsList;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setActive(btnReservations);

        // ✅ cacher les boutons admin (MAINTENANT ils existent dans le FXML)
        hideAdminButtonsForClient();

        Integer clientId = SessionContext.getCurrentUserId();
        if (clientId == null || !SessionContext.isClient()) {
            new Alert(Alert.AlertType.WARNING,
                    "Aucun client connecté (mode test).\n" +
                            "Dans MainFX, appelle SessionContext.loginAsClient(id)."
            ).showAndWait();
            return;
        }

        loadMyReservations(clientId);
    }

    private void hideAdminButtonsForClient() {
        if (btnClients != null) { btnClients.setVisible(false); btnClients.setManaged(false); }
        if (btnHebergement != null) { btnHebergement.setVisible(false); btnHebergement.setManaged(false); }
        if (btnActivites != null) { btnActivites.setVisible(false); btnActivites.setManaged(false); }
    }

    private void loadMyReservations(int clientId) {
        if (reservationsList == null) return;

        reservationsList.getChildren().clear();

        List<Reservation> reservations = sr.getByClientId(clientId);
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets));
        }
    }

    private VBox buildReservationCard(Reservation r, List<Billet> billets) {
        VBox card = new VBox(14);
        card.getStyleClass().add("big-card");

        // Title + badge
        HBox titleRow = new HBox(10);
        Label title = new Label("Réservation #" + r.getIdReservation());
        title.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(safe(r.getStatut()));
        badge.getStyleClass().add(getBadgeClass(r.getStatut()));

        titleRow.getChildren().addAll(title, spacer, badge);

        // meta
        HBox meta = new HBox(18);
        Label date = new Label(r.getDateReservation() != null ? r.getDateReservation().format(df) : "");
        date.getStyleClass().add("muted");

        Label pay = new Label(safe(r.getModalitesPaiement()));
        pay.getStyleClass().add("muted");

        meta.getChildren().addAll(date, pay);

        Label billetsTitle = new Label("Billets (" + (billets == null ? 0 : billets.size()) + ")");
        billetsTitle.getStyleClass().add("section-mini");

        card.getChildren().addAll(titleRow, meta, billetsTitle);

        double total = 0;
        if (billets != null) {
            for (Billet b : billets) {
                total += b.getPrix();
                card.getChildren().add(buildTicketRow(b));
            }
        }

        Separator sep = new Separator();

        HBox totalRow = new HBox(12);
        totalRow.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label("Total de la réservation");
        totalLabel.getStyleClass().add("section-mini");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label totalValue = new Label(String.format("%.0f DT", total));
        totalValue.getStyleClass().add("ticket-price");

        totalRow.getChildren().addAll(totalLabel, sp, totalValue);

        card.getChildren().addAll(sep, totalRow);
        return card;
    }

    private HBox buildTicketRow(Billet b) {
        HBox row = new HBox(14);
        row.getStyleClass().add("ticket-row");

        StackPane icon = new StackPane();
        icon.getStyleClass().add("ticket-icon");

        Label ic = new Label(getIconForTransport(b.getTypeTransport()));
        ic.getStyleClass().add("ticket-icon-text");
        icon.getChildren().add(ic);

        VBox left = new VBox(4);

        Label title = new Label(safe(b.getNumeroBillet()));
        title.getStyleClass().add("ticket-title");

        Label meta1 = new Label("Transport: " + safe(b.getTypeTransport()));
        meta1.getStyleClass().add("muted-small");

        String depart = b.getDateDepart() != null ? b.getDateDepart().format(df) : "";
        String arrivee = b.getDateArrivee() != null ? b.getDateArrivee().format(df) : "";
        Label meta2 = new Label("Départ: " + depart + " → Arrivée: " + arrivee);
        meta2.getStyleClass().add("muted-small");

        left.getChildren().addAll(title, meta1, meta2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(8);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label price = new Label(String.format("%.0f DT", b.getPrix()));
        price.getStyleClass().add("ticket-price");

        Label badge = new Label(safe(b.getStatut()));
        badge.getStyleClass().add(getBadgeClass(b.getStatut())); // jaune/vert/rouge selon statut

        right.getChildren().addAll(price, badge);

        row.getChildren().addAll(icon, left, spacer, right);
        return row;
    }

    // ✅ Utilise TES classes CSS existantes
    private String getBadgeClass(String statut) {
        if (statut == null) return "badge-neutral";
        String s = statut.toLowerCase();
        if (s.contains("attente")) return "badge-waiting";            // jaune/orange
        if (s.contains("confirm")) return "badge-success";            // vert
        if (s.contains("annul") || s.contains("refus")) return "badge-danger"; // rouge
        return "badge-neutral";
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

    private String safe(String s) { return s == null ? "" : s; }

    /* ---------------- NAV ---------------- */

    private void setActive(Button active) {
        Button[] all = {btnHome, btnReservations, btnAvis};
        for (Button b : all) if (b != null) b.getStyleClass().remove("nav-item-active");
        if (active != null && !active.getStyleClass().contains("nav-item-active"))
            active.getStyleClass().add("nav-item-active");
    }

    @FXML private void goHome(ActionEvent e) {
        setActive(btnHome);
        switchScene(e, "/fxml/test.fxml");
    }

    @FXML private void goMyReservations(ActionEvent e) {
        setActive(btnReservations);
        // déjà ici
    }

    @FXML private void goAvis(ActionEvent e) {
        setActive(btnAvis);
        switchScene(e, "/fxml/avis.fxml");
    }

    // Boutons cachés (client) : handlers juste pour éviter erreur FXML
    @FXML private void goClients(ActionEvent e) { }
    @FXML private void goHebergement(ActionEvent e) { }
    @FXML private void goActivites(ActionEvent e) { }

    private void switchScene(ActionEvent e, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.toString()).showAndWait();
        }
    }
}