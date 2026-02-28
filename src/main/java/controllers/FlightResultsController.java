package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBox;
import models.Billet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import javafx.scene.Node;

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
    private Label lblFlightPrice1;

    @FXML
    private Label lblFlightPrice2;

    private DashboardController mainController;
    private String searchDateStr;

    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    public void setRouteInfo(String from, String to, String date) {
        this.searchDateStr = date;
        lblRouteTitle.setText("Vols de " + from + " vers " + to);
        lblDateSubtitle.setText("Aller simple • 1 adulte • " + date);
        generateRandomPrices();
    }

    private void generateRandomPrices() {
        java.util.Random random = new java.util.Random();
        
        // Base prices
        int basePrice = 200 + random.nextInt(300); // 200 - 500
        int diffCheapest = random.nextInt(50);
        int diffFastest = 100 + random.nextInt(200);
        
        int cheapest = basePrice;
        int meilleur = basePrice + 20;
        int fastest = basePrice + diffFastest;
        
        // Update category labels
        if (lblPriceMoinsCher != null) lblPriceMoinsCher.setText(cheapest + " €");
        if (lblPriceMeilleur != null) lblPriceMeilleur.setText(meilleur + " €");
        if (lblPricePlusRapide != null) lblPricePlusRapide.setText(fastest + " €");
        
        // Update flight cards with decimals for realism
        if (lblFlightPrice1 != null) {
            double p1 = meilleur + random.nextDouble() * 0.99;
            lblFlightPrice1.setText(String.format("%.2f €", p1));
        }
        if (lblFlightPrice2 != null) {
            double p2 = meilleur + 25 + random.nextDouble() * 0.99;
            lblFlightPrice2.setText(String.format("%.2f €", p2));
        }
    }

    @FXML
    void handleBackToResearch(ActionEvent event) {
        if (mainController != null) {
            mainController.showSearchInterface();
        }
    }

    @FXML
    void handleCategoryClick(MouseEvent event) {
        VBox clickedBox = (VBox) event.getSource();
        HBox parent = (HBox) clickedBox.getParent();
        
        // Reset all categories
        for (Node node : parent.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("result-option-active");
            }
        }
        
        // Activate clicked category
        clickedBox.getStyleClass().add("result-option-active");
        
        String categoryName = ((Label)clickedBox.getChildren().get(0)).getText();
        System.out.println("Flight Category selected in standalone view: " + categoryName);
        
        // In a real app, we would re-sort or filter the flightsListContainer here
    }

    @FXML
    void handleSelectFlight(ActionEvent event) {
        if (mainController != null) {
            System.out.println("Flight selected -> Triggering new reservation dialog");
            
            // Extract info from the UI to create a mock Billet
            Button btn = (Button) event.getSource();
            VBox priceContainer = (VBox) btn.getParent();
            Label priceLbl = (Label) priceContainer.getChildren().get(0);
            
            String priceStr = priceLbl.getText().replace(" €", "").replace(",", ".").trim();
            double price = 0;
            try { price = Double.parseDouble(priceStr); } catch(Exception ignored){}

            // Try to find airline name by walking up the tree
            String airline = "Inconnu";
            String fp = "AIR-1234";
            try {
                HBox cardHBox = (HBox) priceContainer.getParent();
                VBox airlineBox = (VBox) cardHBox.getChildren().get(0);
                Label airlineLbl = (Label) airlineBox.getChildren().get(0);
                Label fpLbl = (Label) airlineBox.getChildren().get(1);
                airline = airlineLbl.getText();
                fp = fpLbl.getText().replace(" ", "").toUpperCase() + "-" + System.currentTimeMillis()%10000;
            } catch (Exception ignored) { }

            // Try to find exact departure and arrival times from UI
            String depTimeStr = "10:00";
            String arrTimeStr = "12:00";
            try {
                HBox cardHBox = (HBox) priceContainer.getParent();
                VBox middleBox = (VBox) cardHBox.getChildren().get(1); // The middle Box
                HBox timesHBox = (HBox) middleBox.getChildren().get(0); // The HBox holding dep, duration, arr
                
                VBox depBox = (VBox) timesHBox.getChildren().get(0);
                Label lblDepTime = (Label) depBox.getChildren().get(0);
                depTimeStr = lblDepTime.getText().trim();
                
                VBox arrBox = (VBox) timesHBox.getChildren().get(2);
                Label lblArrTime = (Label) arrBox.getChildren().get(0);
                arrTimeStr = lblArrTime.getText().trim();
            } catch (Exception e) {
                System.out.println("Could not extract exact times from UI: " + e.getMessage());
            }

            // Parse date & times
            LocalDateTime depDateTime = LocalDateTime.now().plusDays(5);
            LocalDateTime arrDateTime = LocalDateTime.now().plusDays(5).plusHours(2);
            
            try {
                // Ensure time strings are HH:mm
                if (depTimeStr.length() == 4) depTimeStr = "0" + depTimeStr;
                if (arrTimeStr.length() == 4) arrTimeStr = "0" + arrTimeStr;
                
                LocalTime depTime = LocalTime.parse(depTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime arrTime = LocalTime.parse(arrTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                
                LocalDate baseDate;
                try {
                    // It comes as dd/MM/yyyy or yyyy-MM-dd depending on the DatePicker's output
                    if (this.searchDateStr.contains("-")) {
                        baseDate = LocalDate.parse(this.searchDateStr);
                    } else {
                        baseDate = LocalDate.parse(this.searchDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                } catch (Exception dateEx) {
                    baseDate = LocalDate.now().plusDays(5); // fallback but keeps correct times
                }
                
                depDateTime = LocalDateTime.of(baseDate, depTime);
                arrDateTime = LocalDateTime.of(baseDate, arrTime);
                
                // If arrival time is earlier than departure time, it means the flight arrives the next day
                if (arrTime.isBefore(depTime)) {
                    arrDateTime = arrDateTime.plusDays(1);
                }
            } catch (Exception e) {
                System.out.println("Could not parse date/time string: " + e.getMessage());
            }

            Billet flightBillet = new Billet();
            flightBillet.setTypeTransport("avion");
            flightBillet.setNumeroBillet(fp);
            flightBillet.setPrix(price * 3.3); // convert EUR roughly to DT
            flightBillet.setDateDepart(depDateTime);
            flightBillet.setDateArrivee(arrDateTime);
            flightBillet.setStatut("confirme");

            mainController.triggerNewReservation(flightBillet);
        }
    }
}
