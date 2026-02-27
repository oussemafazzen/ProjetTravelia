package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import models.Hebergement;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HebergementStatsController {

    @FXML
    private BarChart<String, Number> barChartStats;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public void setData(List<Hebergement> hebergements) {
        if (hebergements == null || hebergements.isEmpty()) {
            return;
        }

        // Map: Country -> (Type -> Count)
        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        for (Hebergement h : hebergements) {
            String country = h.getPays() != null ? h.getPays() : "Inconnu";
            String rawType = h.getType() != null ? h.getType().toLowerCase() : "autre";
            String type = (rawType.contains("hotel") || rawType.contains("hôtel")) ? "Hôtels" : "Auberges";

            dataMap.putIfAbsent(country, new HashMap<>());
            Map<String, Integer> typeCounts = dataMap.get(country);
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }

        XYChart.Series<String, Number> hotelSeries = new XYChart.Series<>();
        hotelSeries.setName("Hôtels");
        
        XYChart.Series<String, Number> aubergeSeries = new XYChart.Series<>();
        aubergeSeries.setName("Auberges");

        for (String country : dataMap.keySet()) {
            Map<String, Integer> counts = dataMap.get(country);
            hotelSeries.getData().add(new XYChart.Data<>(country, counts.getOrDefault("Hôtels", 0)));
            aubergeSeries.getData().add(new XYChart.Data<>(country, counts.getOrDefault("Auberges", 0)));
        }

        // Setup Entry Animation State
        barChartStats.setOpacity(0);
        barChartStats.setScaleX(0.9);
        barChartStats.setScaleY(0.9);
        barChartStats.getData().clear();

        Platform.runLater(() -> {
            barChartStats.getData().addAll(hotelSeries, aubergeSeries);

            // Force Integer Graduation (1, 2, 3...)
            int maxVal = 0;
            for (XYChart.Data<String, Number> data : hotelSeries.getData()) maxVal = Math.max(maxVal, data.getYValue().intValue());
            for (XYChart.Data<String, Number> data : aubergeSeries.getData()) maxVal = Math.max(maxVal, data.getYValue().intValue());

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(Math.max(5, maxVal + 1));
            yAxis.setTickUnit(1);
            yAxis.setMinorTickVisible(false);

            // Entry Animations
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), barChartStats);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(600), barChartStats);
            scaleUp.setFromX(0.9);
            scaleUp.setFromY(0.9);
            scaleUp.setToX(1);
            scaleUp.setToY(1);

            fadeIn.play();
            scaleUp.play();
        });
    }
}
