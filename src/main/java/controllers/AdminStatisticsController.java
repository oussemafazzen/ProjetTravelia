package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import services.ClientService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminStatisticsController implements Initializable {

    @FXML
    private PieChart pieChart;

    private ClientService clientService = new ClientService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            Map<String, Integer> stats = clientService.getStatsNiveau();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }

            pieChart.setData(pieChartData);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de données");
            alert.setHeaderText("Impossible de charger les statistiques");
            alert.setContentText("Erreur base de données : " + e.getMessage());
            alert.showAndWait();
        }
    }
}
