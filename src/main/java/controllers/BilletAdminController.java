package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Billet;
import services.ServiceBillet;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BilletAdminController {

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private TableView<Billet> tableBillet;

    @FXML private TableColumn<Billet, Integer> colId;
    @FXML private TableColumn<Billet, String> colNumero;
    @FXML private TableColumn<Billet, String> colTransport;
    @FXML private TableColumn<Billet, String> colDepart;
    @FXML private TableColumn<Billet, String> colArrivee;
    @FXML private TableColumn<Billet, Double> colPrix;
    @FXML private TableColumn<Billet, String> colStatut;
    @FXML private TableColumn<Billet, Integer> colReservation;
    @FXML private TableColumn<Billet, Void> colActions;

    private final ServiceBillet sb = new ServiceBillet();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private ObservableList<Billet> billetList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        refreshTable();

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filterTable(newVal);
        });
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idBillet"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroBillet"));
        colTransport.setCellValueFactory(new PropertyValueFactory<>("typeTransport"));
        
        colDepart.setCellValueFactory(cell -> {
            Billet b = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(b.getDateDepart() != null ? b.getDateDepart().format(dtf) : "-");
        });
        
        colArrivee.setCellValueFactory(cell -> {
            Billet b = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(b.getDateArrivee() != null ? b.getDateArrivee().format(dtf) : "-");
        });
        
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colPrix.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.valueOf((int) prix.doubleValue())); // Troncature pour voir 481 au lieu de 481.79
                }
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colReservation.setCellValueFactory(new PropertyValueFactory<>("reservationId"));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnBlock = new Button("Bloc/Débloq");
            private final HBox box = new HBox(8, btnDelete, btnBlock);
            {
                box.setAlignment(Pos.CENTER);
                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                btnBlock.getStyleClass().add("btn-secondary");
                btnBlock.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");

                btnDelete.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    handleDelete(b);
                });

                btnBlock.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    handleToggleBlock(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void refreshTable() {
        List<Billet> all = sb.getAll();
        billetList.setAll(all);
        tableBillet.setItems(billetList);
        lblTotal.setText(String.valueOf(all.size()));
    }

    private void filterTable(String q) {
        if (q == null || q.isEmpty()) {
            tableBillet.setItems(billetList);
            return;
        }
        String lower = q.toLowerCase();
        List<Billet> filtered = billetList.stream()
                .filter(b -> (b.getNumeroBillet() != null && b.getNumeroBillet().toLowerCase().contains(lower)) ||
                             (b.getTypeTransport() != null && b.getTypeTransport().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        tableBillet.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    void handleSortByDate(ActionEvent event) {
        if (tableBillet == null) return;
        ObservableList<Billet> items = tableBillet.getItems();
        if (items != null) {
            items.sort((b1, b2) -> {
                if (b1.getDateDepart() == null) return 1;
                if (b2.getDateDepart() == null) return -1;
                return b1.getDateDepart().compareTo(b2.getDateDepart());
            });
        }
    }

    @FXML
    void handleSortByStatut(ActionEvent event) {
        if (tableBillet == null) return;
        ObservableList<Billet> items = tableBillet.getItems();
        if (items != null) {
            items.sort((b1, b2) -> {
                String s1 = b1.getStatut() == null ? "" : b1.getStatut();
                String s2 = b2.getStatut() == null ? "" : b2.getStatut();
                return s1.compareToIgnoreCase(s2);
            });
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        refreshTable();
    }

    private void handleDelete(Billet b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le billet " + b.getNumeroBillet() + " ?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                sb.delete(b.getIdBillet());
                refreshTable();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }

    private void handleToggleBlock(Billet b) {
        if (b == null) return;
        
        try {
            String current = b.getStatut() == null ? "" : b.getStatut().toLowerCase();
            
            if (current.contains("annul")) {
                b.setStatut("confirmé"); // Débloquer
            } else {
                b.setStatut("annulé"); // Bloquer
            }

            sb.update(b);
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur Bloc/Débloq: " + e.getMessage()).show();
        }
    }
}
