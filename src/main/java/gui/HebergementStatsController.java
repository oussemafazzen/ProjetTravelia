package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import models.Hebergement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HebergementStatsController {

    @FXML
    private PieChart pieChartCities;

    public void setData(List<Hebergement> hebergements) {
        if (hebergements == null || hebergements.isEmpty()) {
            return;
        }

        Map<String, Integer> cityCounts = new HashMap<>();

        for (Hebergement h : hebergements) {
            String city = h.getVille() != null ? h.getVille() : "Inconnu";
            cityCounts.put(city, cityCounts.getOrDefault(city, 0) + 1);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : cityCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChartCities.setData(pieChartData);

        // Simple dynamic coloring as in the first version
        applyCustomColors();
    }

    private void applyCustomColors() {
        String[] colors = {
            "#FF5733", "#33FF5 green", "#3357FF", "#FF33FB", "#F3FF33", 
            "#33FFF3", "#8D33FF", "#FF8D33", "#33FF8D", "#FF338D",
            "#5D6D7E", "#2ECC71", "#3498DB", "#9B59B6", "#F1C40F",
            "#E67E22", "#E74C3C", "#1ABC9C", "#27AE60", "#2980B9",
            "#8E44AD", "#F39C12", "#D35400", "#C0392B"
        };

        int colorIndex = 0;
        for (PieChart.Data data : pieChartCities.getData()) {
            String color = colors[colorIndex % colors.length];
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
            colorIndex++;
        }
    }
}
