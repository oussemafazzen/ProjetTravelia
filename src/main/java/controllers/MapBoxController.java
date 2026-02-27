package controllers;

import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MapBoxController implements Initializable {

    @FXML
    private WebView webView;

    private WebEngine webEngine;
    private String pendingJsonData = "[]";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webEngine = webView.getEngine();
        
        // Load the local HTML file for the map
        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("Could not find map.html!");
        }

        // Wait for page to finish loading before injecting data
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    injectData(pendingJsonData);
                }
            }
        });
        
        // Add a cool scale-in animation on initialization
        applyPopInAnimation();
    }
    
    private void applyPopInAnimation() {
        webView.setScaleX(0.8);
        webView.setScaleY(0.8);
        webView.setOpacity(0.0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600), webView);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.millis(600), webView);
        fadeTransition.setToValue(1.0);
        
        scaleTransition.play();
        fadeTransition.play();
    }

    public void setHebergementsData(String jsonData) {
        this.pendingJsonData = jsonData;
        // If the page is already loaded, inject right away just in case
        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            injectData(jsonData);
        }
    }

    private void injectData(String jsonData) {
        try {
            // Escape single quotes just in case, though standard JSON escapes them or uses double quotes
            // executeScript takes a JS string. We pass the whole JSON string safely wrapped.
            String script = "loadMarkers('" + jsonData.replace("'", "\\'") + "');";
            webEngine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Error injecting map data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
