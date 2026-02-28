package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.services.ServiceReservation;

import java.util.List;

public class StatsController {

    @FXML private Label lblTotalReservations;
    @FXML private Label lblTotalBillets;
    @FXML private Label lblMontantTotal;

    @FXML private BarChart<String, Number> barPays;
    @FXML private PieChart pieStatut;
    @FXML private LineChart<String, Number> lineMois;

    private final ServiceReservation sr = new ServiceReservation();

    @FXML
    public void initialize() {
        loadAll();
    }

    @FXML
    private void onRefresh(ActionEvent e) {
        loadAll();
    }

    @FXML
    private void onBack(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/admin.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Retour impossible: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void loadAll() {
        try {
            int totalRes = sr.countAll();
            int totalBil = sr.countBilletsAll();
            double totalAmount = sr.sumBilletsAll();

            lblTotalReservations.setText(String.valueOf(totalRes));
            lblTotalBillets.setText(String.valueOf(totalBil));
            lblMontantTotal.setText(String.format("%.0f DT", totalAmount));

            // PIE
            List<Object[]> sRows = sr.countReservationsByStatut();
            var pie = FXCollections.<PieChart.Data>observableArrayList();
            for (Object[] r : sRows) {
                pie.add(new PieChart.Data(String.valueOf(r[0]), ((Number) r[1]).intValue()));
            }
            pieStatut.setData(pie);

            // BAR
            barPays.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            List<Object[]> pRows = sr.topPays(8);
            for (Object[] r : pRows) {
                s.getData().add(new XYChart.Data<>(String.valueOf(r[0]), ((Number) r[1]).intValue()));
            }
            barPays.getData().add(s);
            barPays.setLegendVisible(false);

            // LINE
            lineMois.getData().clear();
            XYChart.Series<String, Number> l = new XYChart.Series<>();
            List<Object[]> mRows = sr.reservationsParMois();
            for (Object[] r : mRows) {
                l.getData().add(new XYChart.Data<>(String.valueOf(r[0]), ((Number) r[1]).intValue()));
            }
            lineMois.getData().add(l);
            lineMois.setLegendVisible(false);

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur stats: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}