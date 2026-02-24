package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Hebergement;
import services.HebergementService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CountrySelectorController {

    @FXML private FlowPane countryFlowPane;

    private HebergementFrontController parentController;
    private HebergementService hs = new HebergementService();

    public void setParentController(HebergementFrontController parentController) {
        this.parentController = parentController;
        loadCountries();
    }

    private void loadCountries() {
        // Get unique countries from available accommodations
        List<Hebergement> all = hs.getAll();
        Set<String> countries = all.stream()
                .map(Hebergement::getPays)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toSet());

        countryFlowPane.getChildren().clear();

        for (String country : countries) {
            Button btn = new Button(country);
            btn.getStyleClass().add("country-selector-item");
            btn.setOnAction(e -> {
                parentController.onCountrySelected(country);
                handleClose();
            });
            countryFlowPane.getChildren().add(btn);
        }
    }

    @FXML
    private void handleResetFilter() {
        parentController.onCountrySelected(null);
        handleClose();
    }

    @FXML
    private void handleClose() {
        ((Stage) countryFlowPane.getScene().getWindow()).close();
    }
}
