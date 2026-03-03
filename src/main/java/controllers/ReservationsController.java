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
import models.DestinationRecommendation;
import services.ServiceBillet;
import services.ServiceReservation;
import services.RecommendationService;
import utils.SessionContext;
import javafx.geometry.Insets;

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

    @FXML private VBox boxRecommendations;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();
    private final RecommendationService recoService = new RecommendationService();

    private final DateTimeFormatter df  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String customBilletTitle = null;
    private String preSelectedDestination = null;
    private DashboardController mainController;

    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    public void setPreSelectedDestination(String dest) {
        this.preSelectedDestination = dest;
    }

    public void setCustomBilletTitle(String title) {
        this.customBilletTitle = title;
        // Optionally reload the list
        if (!SessionContext.isAdmin() && SessionContext.getCurrentUserId() != null) {
            loadReservationsClient(SessionContext.getCurrentUserId());
        }
    }

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
    public void openNewReservationDialog(models.Billet autoBillet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservationDialog.fxml"));
            Parent root = loader.load();

            NewReservationDialogController ctrl = loader.getController();

            // Client ID logic ...
            if (SessionContext.isAdmin()) {
                ctrl.setClientId(1); 
            } else {
                Integer uid = SessionContext.getCurrentUserId();
                if (uid != null) ctrl.setClientId(uid);
            }
            
            if (autoBillet != null) {
                ctrl.setInitialBillet(autoBillet);
            }
            if (preSelectedDestination != null) {
                ctrl.setPaysDestination(preSelectedDestination);
            }

            ctrl.setOnSaved(r -> {
                if (SessionContext.isAdmin()) loadAllReservationsAdmin();
                else loadReservationsClient(SessionContext.getCurrentUserId());
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            
            if (reservationsList != null && reservationsList.getScene() != null) {
                stage.initOwner(reservationsList.getScene().getWindow());
            }
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture:\n" + ex.getMessage());
        }
    }

    @FXML
    void onNewReservation(ActionEvent event) {
        openNewReservationDialog(null);
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
        boolean isFirst = true;
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets, isFirst));
            isFirst = false;
        }

        if (reservations.isEmpty()) addEmpty();
    }

    private void loadReservationsClient(int clientId) {
        reservationsList.getChildren().clear();

        List<Reservation> reservations = sr.getByClientId(clientId);
        boolean isFirst = true;
        for (Reservation r : reservations) {
            List<Billet> billets = sb.getByReservationId(r.getIdReservation());
            reservationsList.getChildren().add(buildReservationCard(r, billets, isFirst));
            isFirst = false;
        }

        if (reservations.isEmpty()) addEmpty();

        loadRecommendations(clientId);
    }

    private void addEmpty() {
        Label empty = new Label("Aucune réservation pour le moment.");
        empty.getStyleClass().add("muted");
        reservationsList.getChildren().add(empty);
    }

    private void loadRecommendations(int clientId) {
        if (boxRecommendations == null) return;
        
        boxRecommendations.setVisible(true);
        boxRecommendations.setManaged(true);
        boxRecommendations.getChildren().clear();

        // 1. Create the main wrapper card
        VBox mainCard = new VBox(15);
        mainCard.getStyleClass().add("big-card");
        mainCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-background-radius: 12;");

        // 2. Header Row: Title + Refresh Button
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("✨");
        iconLbl.setStyle("-fx-font-size: 18px;");
        Label titleLbl = new Label("Recommandations pour vous");
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1e293b;");
        titleBox.getChildren().addAll(iconLbl, titleLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblRefresh = new Label("Rafraîchir");
        lblRefresh.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        lblRefresh.setOnMouseEntered(e -> lblRefresh.setStyle("-fx-text-fill: #4f46e5; -fx-background-color: #e2e8f0; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 13px;"));
        lblRefresh.setOnMouseExited(e -> lblRefresh.setStyle("-fx-text-fill: #6366f1; -fx-background-color: #f1f5f9; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 13px;"));
        
        lblRefresh.setOnMouseClicked(e -> {
            lblRefresh.setText("Mise à jour...");
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), boxRecommendations);
            fade.setFromValue(1.0);
            fade.setToValue(0.2);
            fade.setOnFinished(ev -> {
                loadRecommendations(clientId);
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), boxRecommendations);
                fadeIn.setFromValue(0.2);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fade.play();
        });

        headerRow.getChildren().addAll(titleBox, spacer, lblRefresh);
        mainCard.getChildren().add(headerRow);

        // 3. Get recommendations from AI
        List<DestinationRecommendation> recos = recoService.recommendForClient(clientId, 3);
        
        if (recos == null || recos.isEmpty()) {
            Label noRec = new Label("Aucune suggestion pour le moment. Essayez de rafraîchir !");
            noRec.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 10 0;");
            mainCard.getChildren().add(noRec);
        } else {
            // Subtitle
            Label subtitle = new Label("Basé sur votre historique de réservations (IA).");
            subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            VBox.setMargin(subtitle, new Insets(-10, 0, 5, 0));
            mainCard.getChildren().add(subtitle);

            // Recommendations List
            VBox rowsContainer = new VBox(10);
            for (DestinationRecommendation r : recos) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8; -fx-background-color: #f8fafc;");
                row.setOnMouseEntered(ev -> row.setStyle("-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8; -fx-background-color: #f1f5f9;"));
                row.setOnMouseExited(ev -> row.setStyle("-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8; -fx-background-color: #f8fafc;"));
                
                row.setOnMouseClicked(ev -> openFlightResultsPopup(r.getPays()));

                VBox leftCol = new VBox(4);
                Label rowTitle = new Label(r.getPays());
                rowTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #334155;");
                Label rowReason = new Label(r.getReason());
                rowReason.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                leftCol.getChildren().addAll(rowTitle, rowReason);

                row.getChildren().add(leftCol);
                rowsContainer.getChildren().add(row);
            }
            mainCard.getChildren().add(rowsContainer);
        }

        boxRecommendations.getChildren().add(mainCard);
    }

    // =================== UI ===================

    private VBox buildReservationCard(Reservation r, List<Billet> billets, boolean isFirst) {
        VBox card = new VBox(20);
        card.getStyleClass().add("big-card");

        // --- header
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        String dest = (r.getPaysdestination() != null && !r.getPaysdestination().trim().isEmpty()) 
                        ? r.getPaysdestination() : "destination inconnue";
        
        Label title = new Label("Réservation de Tunis vers " + dest);
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
        String titleText = "Billets (" + billets.size() + ")";
        if (isFirst && customBilletTitle != null && billets.size() > 0) {
            titleText = customBilletTitle;
        }
        Label billetsTitle = new Label(titleText);
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
        btnEdit.getStyleClass().add("gradient-btn");
        btnEdit.setCursor(javafx.scene.Cursor.HAND);
        btnEdit.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #a855f7);"); // Matches "Ajouter billet" vibe but ensures visibility

        Button btnAddBillet = new Button("Ajouter billet");
        btnAddBillet.getStyleClass().add("gradient-btn");
        btnAddBillet.setCursor(javafx.scene.Cursor.HAND);

        Button btnPaiement = new Button("Paiement");
        btnPaiement.getStyleClass().add("gradient-btn");
        btnPaiement.setCursor(javafx.scene.Cursor.HAND);
        btnPaiement.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-weight: bold;");

        btnEdit.setOnAction(ev -> openEditReservationDialog(r));
        btnAddBillet.setOnAction(ev -> openNewBilletDialog(r));
        btnPaiement.setOnAction(ev -> openPaymentDialog(r));

        actions.getChildren().addAll(btnEdit, btnAddBillet, btnPaiement);

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
        if (mainController == null) {
            alert(Alert.AlertType.ERROR, "Erreur interne: mainController est null.");
            return;
        }

        String dep = "Tunis";
        String dest = (r.getPaysdestination() != null && !r.getPaysdestination().isEmpty()) ? r.getPaysdestination() : "Paris";
        
        // Use the DATE of the reservation
        String date = (r.getDateReservation() != null) 
            ? r.getDateReservation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            : java.time.LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        mainController.showFlightResultsIntegrated(
            dep, 
            dest, 
            date, 
            b -> {
                try {
                    b.setReservationId(r.getIdReservation());
                    new ServiceBillet().add(b);
                    System.out.println("New billet added to reservation " + r.getIdReservation());
                    
                    // Return to reservations list and refresh
                    mainController.showReservationsList(null);
                    alert(Alert.AlertType.INFORMATION, "Le nouveau billet a été ajouté avec succès à la réservation #" + r.getIdReservation() + " !");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout du billet: " + ex.getMessage());
                }
            },
            () -> mainController.showReservationsList(null) // Action for Back button
        );
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

    @FXML private void goHome(ActionEvent e) { setActive(btnHome); switchScene(e, "/views/Login.fxml"); }
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

    // =================== PAYMENT ===================

    private void openPaymentDialog(Reservation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PaymentFlightView.fxml"));
            Parent root = loader.load();

            PaymentFlightController ctrl = loader.getController();
            ctrl.setReservation(r);
            ctrl.setOnPaymentSuccess(() -> {
                // Refresh reservation list after payment
                if (SessionContext.isAdmin()) loadAllReservationsAdmin();
                else loadReservationsClient(SessionContext.getCurrentUserId());
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Paiement Réservation #" + r.getIdReservation());

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la page de paiement.");
        }
    }

    private void openFlightResultsPopup(String dest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FlightResultsView.fxml"));
            Parent root = loader.load();

            FlightResultsController frc = loader.getController();
            frc.hideBackButton(); // Hide it here!
            String dep = "Tunis"; // Assuming user is in Tunis
            String date = java.time.LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            // Set callback so "Sélectionner" opens the New Reservation dialog
            frc.setOnBilletSelectedCallback(selectedBillet -> {
                javafx.application.Platform.runLater(() -> {
                    this.preSelectedDestination = dest;
                    openNewReservationDialog(selectedBillet);
                    this.preSelectedDestination = null; // reset after
                });
            });

            // Set route and fetch from SerpApi 
            frc.setRouteInfo(dep, dest, date);
            
            Stage stage = new Stage();
            stage.setTitle("Vols vers " + dest);
            Scene scene = new Scene(root);
            
            var css = getClass().getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur lors du chargement des vols: " + ex.getMessage());
        }
    }
}