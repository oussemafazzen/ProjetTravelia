package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.Avis;
import services.AvisService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AvisAdminController {

    @FXML private TextField tfSearch;
    @FXML private Label lblTotalAvis;
    @FXML private ComboBox<String> cbTypeService;
    @FXML private TableView<Avis> tableAvis;
    
    @FXML private TableColumn<Avis, String> colClient;
    @FXML private TableColumn<Avis, String> colService;
    @FXML private TableColumn<Avis, String> colNote;
    @FXML private TableColumn<Avis, String> colCommentaire;
    @FXML private TableColumn<Avis, String> colDate;
    @FXML private TableColumn<Avis, Void> colPhotos;
    @FXML private TableColumn<Avis, Void> colActions;

    private final AvisService avisService = new AvisService();
    private ObservableList<Avis> masterData = FXCollections.observableArrayList();
    private ObservableList<Avis> filteredData = FXCollections.observableArrayList();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private boolean sortDateAscending = false;
    private boolean sortNoteAscending = false;

    @FXML
    public void initialize() {
        cbTypeService.setItems(FXCollections.observableArrayList(
                "Tous", "Hebergement", "Activite", "Transport"
        ));
        cbTypeService.getSelectionModel().selectFirst();

        setupColumns();
        setupFilters();
        loadData();
    }

    private void setupColumns() {
        colClient.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            if (avis.getClient() != null) {
                return new SimpleStringProperty(avis.getClient().getPrenom() + " " + avis.getClient().getNom());
            }
            return new SimpleStringProperty("Client #" + avis.getIdClient());
        });

        colService.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            return new SimpleStringProperty(avis.getTypeService() + " #" + avis.getIdService());
        });

        colNote.setCellValueFactory(cellData -> {
            int note = cellData.getValue().getNote();
            StringBuilder stars = new StringBuilder();
            for(int i=0; i<note; i++) stars.append("⭐");
            return new SimpleStringProperty(stars.toString() + " (" + note + "/5)");
        });

        colCommentaire.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommentaire()));

        colDate.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            if (avis.getDatePublication() != null) {
                return new SimpleStringProperty(dateFormat.format(avis.getDatePublication()));
            }
            return new SimpleStringProperty("");
        });

        colPhotos.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Avis avis = getTableView().getItems().get(getIndex());
                    int photoCount = (avis.getPhotos() != null) ? avis.getPhotos().size() : 0;
                    if (photoCount > 0) {
                        Button btnPhotos = new Button("📷 " + photoCount);
                        btnPhotos.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                        btnPhotos.setOnAction(e -> showPhotosDialog(avis));
                        setGraphic(btnPhotos);
                        setAlignment(Pos.CENTER);
                    } else {
                        setGraphic(new Label("-"));
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btnDelete = new Button("🗑️ Supprimer");
                    btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> {
                        Avis avis = getTableView().getItems().get(getIndex());
                        deleteAvis(avis);
                    });

                    HBox box = new HBox(btnDelete);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupFilters() {
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbTypeService.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadData() {
        try {
            masterData.setAll(avisService.getAll());
            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les avis: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String keyword = tfSearch.getText().toLowerCase();
        String typeFilter = cbTypeService.getValue();

        List<Avis> filtered = masterData.stream().filter(avis -> {
            // Filter by type
            boolean matchesType = "Tous".equals(typeFilter) || avis.getTypeService().equalsIgnoreCase(typeFilter);
            if (!matchesType) return false;

            // Filter by keyword (client name or comment)
            if (keyword.isEmpty()) return true;
            
            String name = "";
            if (avis.getClient() != null) {
                name = (avis.getClient().getPrenom() + " " + avis.getClient().getNom()).toLowerCase();
            }
            String comment = avis.getCommentaire().toLowerCase();
            String serviceType = avis.getTypeService().toLowerCase();

            return name.contains(keyword) || comment.contains(keyword) || serviceType.contains(keyword);
        }).collect(Collectors.toList());

        filteredData.setAll(filtered);
        tableAvis.setItems(filteredData);
        lblTotalAvis.setText(String.valueOf(filteredData.size()));
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleSortByDate() {
        sortDateAscending = !sortDateAscending;
        Comparator<Avis> comparator = Comparator.comparing(Avis::getDatePublication);
        if (!sortDateAscending) {
            comparator = comparator.reversed();
        }
        FXCollections.sort(filteredData, comparator);
    }

    @FXML
    private void handleSortByNote() {
        sortNoteAscending = !sortNoteAscending;
        Comparator<Avis> comparator = Comparator.comparingInt(Avis::getNote);
        if (!sortNoteAscending) {
            comparator = comparator.reversed();
        }
        FXCollections.sort(filteredData, comparator);
    }

    private void deleteAvis(Avis avis) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet avis et ses photos ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    avisService.delete(avis);
                    masterData.remove(avis);
                    filteredData.remove(avis);
                    lblTotalAvis.setText(String.valueOf(filteredData.size()));
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "La suppression a échoué: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showPhotosDialog(Avis avis) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Photos attachées");
        alert.setHeaderText("Photos pour l'avis #" + avis.getIdAvis());
        
        StringBuilder details = new StringBuilder();
        avis.getPhotos().forEach(p -> details.append("- ").append(p.getCheminFichier()).append(" (").append(p.getLegende()).append(")\n"));
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
