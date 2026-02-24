package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import models.ReservationHebergement;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationStatsController {

    @FXML
    private PieChart pieChartSeasons;
    @FXML
    private Label lblStatus;

    public void setData(List<ReservationHebergement> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            lblStatus.setText("Aucune donnée disponible.");
            pieChartSeasons.setData(FXCollections.observableArrayList());
            return;
        }

        // Filter: Only confirmed reservations
        List<ReservationHebergement> filtered = reservations.stream()
                .filter(r -> r.getStatut() != null && r.getStatut().toLowerCase().contains("confirm"))
                .collect(Collectors.toList());
        
        if (filtered.isEmpty()) {
            lblStatus.setText("Aucune réservation confirmée trouvée.");
            pieChartSeasons.setData(FXCollections.observableArrayList());
            return;
        }

        Map<String, Integer> seasonalCounts = new LinkedHashMap<>();
        seasonalCounts.put("Printemps 🌸", 0);
        seasonalCounts.put("Été ☀️", 0);
        seasonalCounts.put("Automne 🍂", 0);
        seasonalCounts.put("Hiver ❄️", 0);

        for (ReservationHebergement res : filtered) {
            LocalDate date = res.getDateDebut().toLocalDate();
            int month = date.getMonthValue();

            String season;
            if (month >= 3 && month <= 5) {
                season = "Printemps 🌸";
            } else if (month >= 6 && month <= 8) {
                season = "Été ☀️";
            } else if (month >= 9 && month <= 11) {
                season = "Automne 🍂";
            } else {
                season = "Hiver ❄️";
            }

            seasonalCounts.put(season, seasonalCounts.get(season) + 1);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : seasonalCounts.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        pieChartSeasons.setData(pieData);
        pieChartSeasons.setTitle("Demande Saisonnière (Réservations Confirmées)");
        
        lblStatus.setText("Statistiques basées sur " + filtered.size() + " réservations confirmées.");

        // Entry Animation State
        pieChartSeasons.setOpacity(0);
        pieChartSeasons.setScaleX(0.85);
        pieChartSeasons.setScaleY(0.85);

        Platform.runLater(() -> {
            // Entry Animations
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), pieChartSeasons);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(600), pieChartSeasons);
            scaleUp.setFromX(0.85);
            scaleUp.setFromY(0.85);
            scaleUp.setToX(1);
            scaleUp.setToY(1);

            fadeIn.play();
            scaleUp.play();
        });
    }
}
