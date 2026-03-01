package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Billet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.Node;
import services.FlightSearchService;
import java.util.List;

public class FlightResultsController {

    @FXML
    private Button btnBack;

    @FXML
    private Label lblRouteTitle;

    @FXML
    private Label lblDateSubtitle;

    @FXML
    private VBox catMeilleur;

    @FXML
    private VBox catMoinsCher;

    @FXML
    private VBox catPlusRapide;

    @FXML
    private VBox flightsListContainer;

    @FXML
    private Label lblPriceMeilleur;

    @FXML
    private Label lblPriceMoinsCher;

    @FXML
    private Label lblPricePlusRapide;

    @FXML
    private Label lblFlightPrice2;

    @FXML
    private Label lblDurationMeilleur;

    @FXML
    private Label lblDurationMoinsCher;

    @FXML
    private Label lblDurationPlusRapide;

    private DashboardController mainController;
    private List<Billet> allFlights;
    private final FlightSearchService flightSearchService = new FlightSearchService();
    private java.util.function.Consumer<Billet> onBilletSelectedCallback;
    private Runnable onBackAction;

    public void hideBackButton() {
        if (btnBack != null) {
            System.out.println("DEBUG: Physically removing btnBack from parent.");
            if (btnBack.getParent() instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) btnBack.getParent()).getChildren().remove(btnBack);
            } else {
                btnBack.setVisible(false);
                btnBack.setManaged(false);
            }
        } else {
            System.out.println("DEBUG: btnBack is NULL, cannot hide/remove.");
        }
    }

    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    public void setOnBilletSelectedCallback(java.util.function.Consumer<Billet> callback) {
        this.onBilletSelectedCallback = callback;
    }

    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
        if (btnBack != null) {
            btnBack.setVisible(true);
            btnBack.setManaged(true);
        }
    }

    public void setRouteInfo(String from, String to, String date) {
        lblRouteTitle.setText("Vols de " + from + " vers " + to);
        lblDateSubtitle.setText("Aller simple • 1 adulte • " + date);
        
        // Fetch real flights dynamically
        fetchAndDisplayFlights(from, to, date);
    }

    private void fetchAndDisplayFlights(String from, String to, String date) {
        // Clear previous results and show loading state if needed
        flightsListContainer.getChildren().clear();
        Label loading = new Label("Recherche des vols en cours...");
        loading.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20 0 0 0;");
        flightsListContainer.getChildren().add(loading);

        new Thread(() -> {
            List<Billet> realFlights = flightSearchService.searchRealFlights(from, to, date);
            
            javafx.application.Platform.runLater(() -> {
                flightsListContainer.getChildren().clear();
                if (realFlights.isEmpty()) {
                    Label noFlights = new Label("Aucun vol trouvé pour cette destination ou date.");
                    noFlights.setStyle("-fx-font-size: 16px; -fx-text-fill: #d9534f; -fx-font-weight: bold; -fx-padding: 20 0 0 0;");
                    flightsListContainer.getChildren().add(noFlights);
                    
                    if (lblPriceMoinsCher != null) lblPriceMoinsCher.setText("N/A");
                    if (lblPriceMeilleur != null) lblPriceMeilleur.setText("N/A");
                    if (lblPricePlusRapide != null) lblPricePlusRapide.setText("N/A");
                    return;
                }

                this.allFlights = realFlights;
                updateUIWithRealFlights(realFlights);
            });
        }).start();
    }

    private void updateUIWithRealFlights(List<Billet> flights) {
        Billet cheapest = null;
        Billet fastest = null;
        
        for (Billet f : flights) {
            if (f.getPrix() <= 0) continue;
            
            if (cheapest == null || f.getPrix() < cheapest.getPrix()) cheapest = f;
            
            long durationMins = java.time.Duration.between(f.getDateDepart(), f.getDateArrivee()).toMinutes();
            if (durationMins <= 0) continue;
            
            if (fastest == null) {
                fastest = f;
            } else {
                long currentBest = java.time.Duration.between(fastest.getDateDepart(), fastest.getDateArrivee()).toMinutes();
                if (durationMins < currentBest) fastest = f;
            }
        }
        
        if (cheapest == null) cheapest = flights.get(0);
        if (fastest == null) fastest = flights.get(0);

        if (lblPriceMoinsCher != null) lblPriceMoinsCher.setText(String.format("%.2f €", cheapest.getPrix()));
        if (lblPriceMeilleur != null) lblPriceMeilleur.setText(String.format("%.2f €", flights.get(0).getPrix()));
        if (lblPricePlusRapide != null) lblPricePlusRapide.setText(String.format("%.2f €", fastest.getPrix()));

        if (lblDurationMoinsCher != null) lblDurationMoinsCher.setText(formatDuration(cheapest));
        if (lblDurationMeilleur != null) lblDurationMeilleur.setText(formatDuration(flights.get(0)));
        if (lblDurationPlusRapide != null) lblDurationPlusRapide.setText(formatDuration(fastest));

        // Create new flight cards dynamically for all results found
        displaySortedFlights(flights);
    }

    private void displaySortedFlights(List<Billet> flights) {
        flightsListContainer.getChildren().clear(); // Properly clear all cards (mock and real)
        for (Billet f : flights) {
            flightsListContainer.getChildren().add(createFlightCard(f));
        }
    }

    /**
     * Creates a simple HBox flight card programmatically to ensure it always works 
     * even if FXML injection fails or if we have many flights.
     */
    private Node createFlightCard(Billet f) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Airline
        VBox airlineBox = new VBox(5);
        airlineBox.setPrefWidth(120);
        
        String rawBillet = f.getNumeroBillet() != null ? f.getNumeroBillet() : "Compagnie|Inconnue";
        String airlineName = "Avion";
        String flightNum = rawBillet;
        
        if (rawBillet.contains("|")) {
            String[] parts = rawBillet.split("\\|");
            airlineName = parts[0];
            if (parts.length > 1) flightNum = parts[1];
        }
        
        Label airline = new Label(airlineName);
        airline.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label num = new Label(flightNum);
        num.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        airlineBox.getChildren().addAll(airline, num);

        // Times
        HBox times = new HBox(30);
        times.setAlignment(javafx.geometry.Pos.CENTER);
        HBox.setHgrow(times, javafx.scene.layout.Priority.ALWAYS);
        
        VBox dep = new VBox(2);
        Label depTime = new Label(f.getDateDepart().format(DateTimeFormatter.ofPattern("HH:mm")));
        depTime.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        dep.getChildren().add(depTime);

        Label arrow = new Label(" ✈ ");
        arrow.setStyle("-fx-font-size: 20px; -fx-text-fill: #4a90e2;");

        VBox arr = new VBox(2);
        Label arrTime = new Label(f.getDateArrivee().format(DateTimeFormatter.ofPattern("HH:mm")));
        arrTime.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        arr.getChildren().add(arrTime);

        times.getChildren().addAll(dep, arrow, arr);

        // Price & Select
        VBox priceBox = new VBox(10);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label price = new Label(String.format("%.2f €", f.getPrix()));
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");
        Button select = new Button("Sélectionner");
        select.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        select.setOnAction(e -> handleSelectFlightForReal(f));
        priceBox.getChildren().addAll(price, select);

        card.getChildren().addAll(airlineBox, times, priceBox);
        return card;
    }

    private void handleSelectFlightForReal(Billet f) {
        System.out.println("Flight selected: " + f.getNumeroBillet());
        
        // Rough conversion to DT for the final reservation/billet
        f.setPrix(f.getPrix() * 3.3);
        f.setStatut("confirme");
        
        // CRITICAL FOR DATABASE: Ensure type transport is explicitly set
        if (f.getTypeTransport() == null || f.getTypeTransport().trim().isEmpty()) {
            f.setTypeTransport("avion");
        }
        
        // CRITICAL FOR DATABASE: Database `numero_billet` is VARCHAR(20). 
        // Some generated/API flight IDs are longer and cause silent constraint failures.
        if (f.getNumeroBillet() != null && f.getNumeroBillet().length() > 20) {
            f.setNumeroBillet(f.getNumeroBillet().substring(0, 20));
        }

        if (onBilletSelectedCallback != null) {
            onBilletSelectedCallback.accept(f);
            // Close the current stage if we are in one
            ((javafx.stage.Stage) flightsListContainer.getScene().getWindow()).close();
            return;
        }

        if (mainController != null) {
            String dest = "Destination";
            try {
                String routeText = lblRouteTitle.getText();
                if (routeText.contains("vers ")) {
                    dest = routeText.substring(routeText.indexOf("vers ") + 5).trim();
                }
                if (dest.contains(" (")) dest = dest.substring(0, dest.indexOf(" (")).trim();
            } catch(Exception e) {}

            mainController.triggerNewReservation(f, dest);
        }
    }

    @FXML
    void handleBackToResearch(ActionEvent event) {
        if (onBackAction != null) {
            onBackAction.run();
        } else if (mainController != null) {
            mainController.showSearchInterface();
        }
    }

    @FXML
    void handleCategoryClick(MouseEvent event) {
        VBox clickedBox = (VBox) event.getSource();
        
        // Visual feedback
        catMeilleur.getStyleClass().remove("result-option-active");
        catMoinsCher.getStyleClass().remove("result-option-active");
        catPlusRapide.getStyleClass().remove("result-option-active");
        clickedBox.getStyleClass().add("result-option-active");
        
        if (allFlights == null || allFlights.isEmpty()) return;
        
        List<Billet> sorted = new java.util.ArrayList<>(allFlights);
        
        if (clickedBox == catMeilleur) {
            System.out.println("Tri : Le meilleur");
            displaySortedFlights(allFlights); // Original order from API is "Best"
        } else if (clickedBox == catMoinsCher) {
            System.out.println("Tri : Le moins cher (Prix ASC)");
            sorted.sort(java.util.Comparator.comparingDouble(Billet::getPrix));
            displaySortedFlights(sorted);
        } else if (clickedBox == catPlusRapide) {
            System.out.println("Tri : Le plus rapide (Durée ASC)");
            sorted.sort((f1, f2) -> {
                long d1 = java.time.Duration.between(f1.getDateDepart(), f1.getDateArrivee()).toMinutes();
                long d2 = java.time.Duration.between(f2.getDateDepart(), f2.getDateArrivee()).toMinutes();
                return Long.compare(d1, d2);
            });
            displaySortedFlights(sorted);
        }
    }

    @FXML
    void handleSelectFlight(ActionEvent event) {
        // Obsolete handles legacy static FXML buttons
    }

    private String formatDuration(Billet b) {
        if (b.getDateDepart() == null || b.getDateArrivee() == null) return "N/A";
        long mins = java.time.Duration.between(b.getDateDepart(), b.getDateArrivee()).toMinutes();
        long h = mins / 60;
        long m = mins % 60;
        return h + " h " + m + " min";
    }
}
