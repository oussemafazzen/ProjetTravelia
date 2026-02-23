package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDeleteController {

    @FXML private Label lblMessage;
    private boolean confirmed = false;

    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleDelete() {
        confirmed = true;
        closeStage();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeStage();
    }

    private void closeStage() {
        if (lblMessage.getScene() != null && lblMessage.getScene().getWindow() != null) {
            ((Stage) lblMessage.getScene().getWindow()).close();
        }
    }
}
