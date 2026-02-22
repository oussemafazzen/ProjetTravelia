package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import models.ReservationHebergement;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationStatsController {

    @FXML
    private PieChart pieChartSeasons;
    @FXML
    private Label lblStatus;

    public void setData(List<ReservationHebergement> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            lblStatus.setText("Aucune donnée disponible pour les statistiques.");
            return;
        }

        Map<String, Integer> seasonalCounts = new HashMap<>();
        seasonalCounts.put("Printemps 🌸", 0);
        seasonalCounts.put("Été ☀️", 0);
        seasonalCounts.put("Automne 🍂", 0);
        seasonalCounts.put("Hiver ❄️", 0);

        for (ReservationHebergement res : reservations) {
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

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : seasonalCounts.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }

        pieChartSeasons.setData(pieChartData);
        lblStatus.setText("Total des réservations analysées : " + reservations.size());
    }
}
