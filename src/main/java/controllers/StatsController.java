package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import services.ClientService;
import services.HebergementService;
import services.ReservationHebergementService;
import services.ServiceReservation;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatsController {

    @FXML private Label lblTotalClients;
    @FXML private Label lblTotalHebergements;
    @FXML private Label lblTotalReservations;
    @FXML private Label lblMontantTotal;

    @FXML private PieChart pieLoyalty;
    @FXML private PieChart pieNationality;
    
    @FXML private PieChart pieHebType;
    @FXML private BarChart<String, Number> barGroupedHebPays;
    
    
    
    @FXML private PieChart pieStatut;
    @FXML private BarChart<String, Number> barPays;
    @FXML private LineChart<String, Number> lineMois;

    private final ServiceReservation sr = new ServiceReservation();
    private final ClientService cs = new ClientService();
    private final HebergementService hs = new HebergementService();


    @FXML
    public void initialize() {
        loadAll();
    }

    @FXML
    private void onRefresh(ActionEvent e) {
        loadAll();
    }

    private void loadAll() {
        try {
            // KPIs
            int totalClients = cs.countAll();
            int totalHeb = hs.countAll();
            int totalResVols = sr.countAll();
            double totalAmount = sr.sumBilletsAll();

            lblTotalClients.setText(String.valueOf(totalClients));
            lblTotalHebergements.setText(String.valueOf(totalHeb));
            lblTotalReservations.setText(String.valueOf(totalResVols));
            lblMontantTotal.setText(String.format("%.0f DT", totalAmount));

            // Section: Clients
            loadClientStats();

            // Section: Hébergements
            loadHebergementStats();

            // Section: Réservations Vols
            loadReservationVolStats();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur stats: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void loadClientStats() throws SQLException {
        // Loyalty
        Map<String, Integer> loyaltyStats = cs.getStatsNiveau();
        ObservableList<PieChart.Data> loyaltyData = FXCollections.observableArrayList();
        loyaltyStats.forEach((niveau, count) -> {
            loyaltyData.add(new PieChart.Data(niveau + " (" + count + ")", count));
        });
        pieLoyalty.setData(loyaltyData);

        // Nationality
        Map<String, Integer> nationalityStats = cs.getStatsNationalite();
        ObservableList<PieChart.Data> nationalityData = FXCollections.observableArrayList();
        nationalityStats.forEach((nat, count) -> {
            nationalityData.add(new PieChart.Data(nat + " (" + count + ")", count));
        });
        pieNationality.setData(nationalityData);
    }

    private void loadHebergementStats() throws SQLException {
        // Types (Overall)
        List<Object[]> typeRows = hs.getStatsType();
        var typeData = FXCollections.<PieChart.Data>observableArrayList();
        for (Object[] r : typeRows) {
            typeData.add(new PieChart.Data(String.valueOf(r[0]), ((Number) r[1]).intValue()));
        }
        pieHebType.setData(typeData);

        // Grouped Bar: Hôtels vs Auberges per Country
        barGroupedHebPays.getData().clear();
        Map<String, Map<String, Integer>> groupedData = hs.getStatsGroupedByPays();
        
        XYChart.Series<String, Number> hotelSeries = new XYChart.Series<>();
        hotelSeries.setName("Hôtels");
        
        XYChart.Series<String, Number> aubergeSeries = new XYChart.Series<>();
        aubergeSeries.setName("Auberges");

        groupedData.forEach((country, types) -> {
            hotelSeries.getData().add(new XYChart.Data<>(country, types.getOrDefault("Hôtels", 0)));
            aubergeSeries.getData().add(new XYChart.Data<>(country, types.getOrDefault("Auberges", 0)));
        });

        barGroupedHebPays.getData().addAll(hotelSeries, aubergeSeries);
    }


    private void loadReservationVolStats() {
        // PIE Statut
        List<Object[]> sRows = sr.countReservationsByStatut();
        var pie = FXCollections.<PieChart.Data>observableArrayList();
        for (Object[] r : sRows) {
            pie.add(new PieChart.Data(String.valueOf(r[0]), ((Number) r[1]).intValue()));
        }
        pieStatut.setData(pie);

        // BAR Pays
        barPays.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        List<Object[]> pRows = sr.topPays(8);
        for (Object[] r : pRows) {
            s.getData().add(new XYChart.Data<>(String.valueOf(r[0]), ((Number) r[1]).intValue()));
        }
        barPays.getData().add(s);
        barPays.setLegendVisible(false);

        // LINE Mois
        lineMois.getData().clear();
        XYChart.Series<String, Number> l = new XYChart.Series<>();
        List<Object[]> mRows = sr.reservationsParMois();
        for (Object[] r : mRows) {
            l.getData().add(new XYChart.Data<>(String.valueOf(r[0]), ((Number) r[1]).intValue()));
        }
        lineMois.getData().add(l);
        lineMois.setLegendVisible(false);
    }
}
