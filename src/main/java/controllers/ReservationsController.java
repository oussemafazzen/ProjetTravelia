package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Billet;
import models.Reservation;
import services.ServiceBillet;
import services.ServiceReservation;
import utils.SessionContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationsController {

    @FXML private Button btnHome;
    @FXML private Button btnClients;
    @FXML private Button btnReservations;
    @FXML private Button btnHebergement;
    @FXML private Button btnActivites;
    @FXML private Button btnAvis;
    @FXML private Button btnCalendar;

    @FXML private VBox reservationsList;
    @FXML private Button btnNewReservation;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();

    private final DateTimeFormatter df  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setActive(btnReservations);

        if (SessionContext.isAdmin()) {
            loadAllReservationsAdmin();
            return;
        }

        Integer clientId = SessionContext.getCurrentUserId();
        if (clientId == null) {
            // SessionContext not initialized (e.g. embedded in FrontOffice)
            // Fall back to showing all reservations
            loadAllReservationsAdmin();
            return;
        }

        loadReservationsClient(clientId);
    }

    // =================== ACTIONS ===================

    // ✅ POPUP Nouvelle réservation
    @FXML
    private void onNewReservation(ActionEvent e) {
        Integer clientId = SessionContext.getCurrentUserId();
        if (clientId == null && !SessionContext.isAdmin()) {
            alert(Alert.AlertType.WARNING, "Aucun client connecté.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservationDialog.fxml"));
            Parent root = loader.load();

            NewReservationDialogController ctrl = loader.getController();
            if (!SessionContext.isAdmin()) ctrl.setClientId(clientId);

            ctrl.setOnSaved(r -> {
                // refresh après save
                if (SessionContext.isAdmin()) loadAllReservationsAdmin();
                else loadReservationsClient(SessionContext.getCurrentUserId());
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.initOwner(((Node) e.getSource()).getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur ouverture popup: " + ex.getMessage());
        }
    }

    // ✅ POPUP CALENDRIER
    @FXML
    private void onOpenCalendar(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CalendarView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Calendrier - Travelia");
            stage.setScene(new Scene(root, 900, 520));

            var css = getClass().getResource("/css/app.css");
            if (css != null) stage.getScene().getStylesheets().add(css.toExternalForm());

            stage.initOwner(((Node) e.getSource()).getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur ouverture calendrier: " + ex.getMessage());
        }
    }

    // =================== LOADERS ===================

    private void loadAllReservationsAdmin() {
        reservationsList.getChildren().clear();

        List<Reservation> reservations = sr.getAll();
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets));
        }

        if (reservations.isEmpty()) addEmpty();
    }

    private void loadReservationsClient(int clientId) {
        reservationsList.getChildren().clear();

        List<Reservation> reservations = sr.getByClientId(clientId);
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets));
        }

        if (reservations.isEmpty()) addEmpty();
    }

    private void addEmpty() {
        Label empty = new Label("Aucune réservation pour le moment.");
        empty.getStyleClass().add("muted");
        reservationsList.getChildren().add(empty);
    }

    // =================== UI ===================

    private VBox buildReservationCard(Reservation r, List<Billet> billets) {
        VBox card = new VBox(20);
        card.getStyleClass().add("big-card");

        // --- header
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Réservation #" + r.getIdReservation());
        title.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(safe(r.getStatut()).toUpperCase());
        badge.getStyleClass().addAll("status-badge", getBadgeClass(r.getStatut()));

        titleRow.getChildren().addAll(title, spacer, badge);

        // --- meta
        VBox metaBox = new VBox(6);
        String dateTxt = (r.getDateReservation() != null) ? r.getDateReservation().format(df) : "-";
        Label date = new Label(dateTxt);
        date.getStyleClass().add("muted");

        Label pay = new Label(safe(r.getModalitesPaiement()));
        pay.getStyleClass().add("muted");

        metaBox.getChildren().addAll(date, pay);

        // --- billets title
        Label billetsTitle = new Label("Billets (" + billets.size() + ")");
        billetsTitle.getStyleClass().add("section-mini");
        VBox.setMargin(billetsTitle, new javafx.geometry.Insets(10, 0, 0, 0));

        card.getChildren().addAll(titleRow, metaBox, billetsTitle);

        // --- billets list
        double total = 0;
        if (billets.isEmpty()) {
            Label noTickets = new Label("Aucun billet associé à cette réservation.");
            noTickets.getStyleClass().add("muted-small");
            card.getChildren().add(noTickets);
        } else {
            for (Billet b : billets) {
                total += b.getPrix();
                card.getChildren().add(buildTicketRow(b));
            }
        }

        Separator sep = new Separator();
        sep.getStyleClass().add("soft-sep");

        // --- total row
        HBox totalRow = new HBox(12);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.getStyleClass().add("total-row");

        Label totalLabel = new Label("Total de la réservation");
        totalLabel.getStyleClass().add("total-label");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label totalValue = new Label(String.format("%.0f DT", total));
        totalValue.getStyleClass().add("total-amount");

        totalRow.getChildren().addAll(totalLabel, sp, totalValue);

        // --- actions row
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("actions-row");

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-outline");
        btnEdit.setCursor(javafx.scene.Cursor.HAND);

        Button btnAddBillet = new Button("Ajouter billet");
        btnAddBillet.getStyleClass().add("gradient-btn");
        btnAddBillet.setCursor(javafx.scene.Cursor.HAND);

        btnEdit.setOnAction(ev -> openEditReservationDialog(r));
        btnAddBillet.setOnAction(ev -> openNewBilletDialog(r));

        actions.getChildren().addAll(btnEdit, btnAddBillet);

        card.getChildren().addAll(sep, totalRow, actions);

        return card;
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

        VBox left = new VBox(6);
        Label title = new Label(safe(b.getNumeroBillet()));
        title.getStyleClass().add("ticket-title");

        Label transport = new Label("Transport: " + safe(b.getTypeTransport()));
        transport.getStyleClass().add("muted-small");

        String depart  = (b.getDateDepart() != null) ? b.getDateDepart().format(dtf) : "-";
        String arrivee = (b.getDateArrivee() != null) ? b.getDateArrivee().format(dtf) : "-";
        Label meta = new Label("Départ: " + depart + " → Arrivée: " + arrivee);
        meta.getStyleClass().add("muted-small");

        left.getChildren().addAll(title, transport, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(8);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label price = new Label(String.format("%.0f DT", b.getPrix()));
        price.getStyleClass().add("ticket-price");

        Label badge = new Label(safe(b.getStatut()).toUpperCase());
        badge.getStyleClass().addAll("status-badge", getBadgeClass(b.getStatut()));

        right.getChildren().addAll(price, badge);

        row.getChildren().addAll(icon, left, spacer, right);
        return row;
    }

    // =================== POPUPS ===================

    private void openEditReservationDialog(Reservation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditReservationDialog.fxml"));
            Parent root = loader.load();

            EditReservationDialogController ctrl = loader.getController();
            ctrl.setReservation(r);

            ctrl.setOnUpdated(updated -> {
                // refresh après update
                if (SessionContext.isAdmin()) loadAllReservationsAdmin();
                else loadReservationsClient(SessionContext.getCurrentUserId());
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier réservation #" + r.getIdReservation());
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur popup modifier: " + ex.getMessage());
        }
    }

    private void openNewBilletDialog(Reservation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewBilletDialog.fxml"));
            Parent root = loader.load();

            NewBilletDialogController ctrl = loader.getController();
            ctrl.setReservationId(r.getIdReservation());

            ctrl.setOnSaved(b -> {
                // refresh après ajout billet
                if (SessionContext.isAdmin()) loadAllReservationsAdmin();
                else loadReservationsClient(SessionContext.getCurrentUserId());
            });

            Stage stage = new Stage();
            stage.setTitle("Ajouter billet (Réservation #" + r.getIdReservation() + ")");
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur popup billet: " + ex.getMessage());
        }
    }

    // =================== UTILS ===================

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String getBadgeClass(String statut) {
        if (statut == null) return "status-pending";
        String s = statut.toLowerCase();
        if (s.contains("attente")) return "status-pending";
        if (s.contains("confirm")) return "status-confirmed";
        if (s.contains("annul")) return "status-cancelled";
        return "status-pending";
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

    private void alert(Alert.AlertType t, String msg) {
        new Alert(t, msg, ButtonType.OK).showAndWait();
    }

    // =================== NAVIGATION ===================

    private void setActive(Button active) {
        Button[] all = {btnHome, btnReservations, btnClients, btnHebergement, btnActivites, btnAvis};
        for (Button b : all) {
            if (b != null) b.getStyleClass().remove("nav-item-active");
        }
        if (active != null) active.getStyleClass().add("nav-item-active");
    }

    @FXML private void goHome(ActionEvent e) { setActive(btnHome); switchScene(e, "/views/Home.fxml"); }
    @FXML private void goReservations(ActionEvent e) { setActive(btnReservations); }
    @FXML private void goClients(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Accès réservé à l'admin."); }
    @FXML private void goHebergement(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Accès réservé à l'admin."); }
    @FXML private void goActivites(ActionEvent e) { alert(Alert.AlertType.INFORMATION, "Accès réservé à l'admin."); }
    @FXML private void goAvis(ActionEvent e) { switchScene(e, "/views/FrontOfficeView.fxml"); } // Placeholder or correct view

    private void switchScene(ActionEvent e, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            Scene scene = new Scene(root, 1300, 800);
            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}