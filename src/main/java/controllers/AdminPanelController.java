package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import models.Client;
import models.User;
import models.enums.NiveauFidelite;
import models.enums.Role;
import models.enums.Statut;
import services.ClientService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminPanelController {

    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colPrenom;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colPhone;
    @FXML private TableColumn<Client, String> colNationality;
    @FXML private TableColumn<Client, java.util.Date> colBirthDate;
    @FXML private TableColumn<Client, Integer> colPoints;
    @FXML private TableColumn<Client, Object> colNiveau;
    @FXML private TableColumn<Client, Object> colStatut;
    @FXML private TableColumn<Client, Void> colActions;

    // Admin Table
    @FXML private VBox adminManagementView;
    @FXML private TableView<Client> tableAdmins;
    @FXML private TableColumn<Client, String> colAdminNom;
    @FXML private TableColumn<Client, String> colAdminPrenom;
    @FXML private TableColumn<Client, String> colAdminEmail;
    @FXML private TableColumn<Client, String> colAdminPhone;
    @FXML private TableColumn<Client, String> colAdminNationality;
    @FXML private TableColumn<Client, java.util.Date> colAdminBirthDate;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblBlockedUsers;
    @FXML private Label lblTotalRevenue;

    @FXML private TextField txtSearch;
    @FXML private PieChart loyaltyPieChart;
    @FXML private PieChart nationalityPieChart;
    @FXML private StackPane mainStackPane;
    @FXML private VBox userManagementView;
    @FXML private VBox statisticsView;
    @FXML private VBox reservationsView;
    @FXML private VBox accommodationsView;
    @FXML private VBox activitiesView;
    @FXML private VBox reviewsView;
    @FXML private Button btnThemeToggle;
    @FXML private AnchorPane rootPane;

    @FXML private VBox sidebarVBox;
    @FXML private Button btnSidebarToggle;
    @FXML private StackPane accommodationsContent;
    @FXML private Button btnNavHebergement;
    @FXML private Button btnNavReservation;

    private boolean isSidebarHidden = false;
    private boolean isLightMode = false;
    private static final double SIDEBAR_WIDTH = 250.0;

    private ClientService clientService = new ClientService();
    private ObservableList<Client> clientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupAdminTable();
        refreshTable();
        
        // Real-time search listener
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTable(newValue);
        });
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationalite"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points_fidelite"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau_fidelite"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        addButtonToTable();
    }

    private void setupAdminTable() {
        colAdminNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAdminPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colAdminEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdminPhone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colAdminNationality.setCellValueFactory(new PropertyValueFactory<>("nationalite"));
        colAdminBirthDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
    }

    private void addButtonToTable() {
        Callback<TableColumn<Client, Void>, TableCell<Client, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Client, Void> call(final TableColumn<Client, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprim.");
                    private final Button btnBlock = new Button("Bloq/Débloq");

                    {
                        btnEdit.getStyleClass().add("btn-outline-primary");
                        btnDelete.getStyleClass().add("btn-outline-danger");
                        btnBlock.getStyleClass().add("btn-warning");
                        btnBlock.setStyle("-fx-padding: 3 8; -fx-font-size: 11px;");

                        btnEdit.setOnAction((ActionEvent event) -> {
                            Client client = getTableView().getItems().get(getIndex());
                            handleEditClient(client);
                        });

                        btnBlock.setOnAction((ActionEvent event) -> {
                            Client client = getTableView().getItems().get(getIndex());
                            try {
                                clientService.blockClient(client.getId());
                                refreshTable();
                            } catch (SQLException e) {
                                showError("Erreur de blocage", e.getMessage());
                            }
                        });

                        btnDelete.setOnAction((ActionEvent event) -> {
                            Client client = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ConfirmDeleteDialog.fxml"));
                                Parent root = loader.load();
                                ConfirmDeleteController controller = loader.getController();
                                controller.setMessage("Supprimer le client " + client.getNom() + " " + client.getPrenom() + " ?");
                                
                                Stage stage = new Stage();
                                stage.initModality(Modality.APPLICATION_MODAL);
                                stage.initStyle(StageStyle.UNDECORATED); // Modern look without window borders
                                stage.setScene(new Scene(root));
                                stage.showAndWait();
                                
                                if (controller.isConfirmed()) {
                                    clientService.delete(client.getId());
                                    refreshTable();
                                }
                            } catch (Exception e) {
                                showError("Erreur de suppression", e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox pane = new HBox(btnEdit, btnDelete, btnBlock);
                            pane.setSpacing(5);
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    @FXML
    void refreshTable() {
        try {
            List<Client> allClients = clientService.getAll().stream()
                    .filter(c -> c.getRole() != Role.ADMINISTRATEUR)
                    .collect(Collectors.toList());
            clientList.setAll(allClients);
            tableClients.setItems(clientList);
            updateQuickStats();
        } catch (SQLException e) {
            showError("Erreur de chargement des données", e.getMessage());
        }
    }

    private void filterTable(String query) {
        if (query == null || query.isEmpty()) {
            refreshTable();
            return;
        }
        try {
            List<Client> filtered = clientService.rechercherParNom(query).stream()
                    .filter(c -> c.getRole() != Role.ADMINISTRATEUR)
                    .collect(Collectors.toList());
            clientList.setAll(filtered);
            tableClients.setItems(clientList);
        } catch (Exception e) {
            showError("Erreur de filtrage", e.getMessage());
        }
    }

    @FXML
    void handleSearch() {
        filterTable(txtSearch.getText());
    }

    @FXML
    void handleSortByPoints() {
        try {
            List<Client> sorted = clientService.trierParPoints().stream()
                    .filter(c -> c.getRole() != Role.ADMINISTRATEUR)
                    .collect(Collectors.toList());
            clientList.setAll(sorted);
            tableClients.setItems(clientList);
        } catch (Exception e) {
            showError("Erreur de tri", e.getMessage());
        }
    }

    @FXML
    void showAdminManagement() {
        switchView(adminManagementView);
        refreshAdminTable();
    }

    private void refreshAdminTable() {
        try {
            List<Client> admins = clientService.getAll().stream()
                    .filter(c -> c.getRole() == Role.ADMINISTRATEUR)
                    .collect(Collectors.toList());
            tableAdmins.setItems(FXCollections.observableArrayList(admins));
        } catch (SQLException e) {
            showError("Erreur de chargement des administrateurs", e.getMessage());
        }
    }

    @FXML
    void showUserManagement() {
        switchView(userManagementView);
        refreshTable();
    }

    @FXML
    void showStatistics() {
        switchView(statisticsView);
        loadStatistics();
    }



    @FXML
    void showReservations() {
        switchView(reservationsView);
    }

    @FXML
    void showAccommodations() {
        switchView(accommodationsView);
        showHebergementsContent();
    }

    @FXML
    void showHebergementsContent() {
        updateSubNavStyles(btnNavHebergement, btnNavReservation);
        loadSubView("/views/HebergementView.fxml", accommodationsContent);
    }

    @FXML
    void showReservationsContent() {
        updateSubNavStyles(btnNavReservation, btnNavHebergement);
        loadSubView("/views/ReservationHebergementView.fxml", accommodationsContent);
    }

    private void loadSubView(String fxmlPath, StackPane container) {
        try {
            container.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            container.getChildren().add(view);
        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue : " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateSubNavStyles(Button active, Button inactive) {
        active.getStyleClass().remove("btn-sub-nav");
        active.getStyleClass().add("btn-sub-nav-active");
        
        inactive.getStyleClass().remove("btn-sub-nav-active");
        inactive.getStyleClass().add("btn-sub-nav");
    }

    @FXML
    void showActivities() {
        switchView(activitiesView);
    }

    @FXML
    void showReviews() {
        switchView(reviewsView);
    }

    @FXML
    void toggleTheme() {
        isLightMode = !isLightMode;
        if (rootPane != null) {
            rootPane.getStylesheets().clear();
            if (isLightMode) {
                rootPane.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
                btnThemeToggle.setText("🌙 Mode Sombre");
            } else {
                rootPane.getStylesheets().add(getClass().getResource("/css/admin-theme.css").toExternalForm());
                btnThemeToggle.setText("☀️ Mode Clair");
            }
        }
    }


    private void updateQuickStats() {
        try {
            List<Client> allClients = clientService.getAll();
            long total = allClients.stream()
                    .filter(c -> c.getRole() == Role.USER || c.getRole() == Role.CLIENT)
                    .count();
            long blocked = allClients.stream().filter(c -> c.getStatut() == Statut.BLOQUE).count();
            long revenue = allClients.stream().mapToLong(Client::getPoints_fidelite).sum();

            lblTotalUsers.setText(String.valueOf(total));
            lblBlockedUsers.setText(String.valueOf(blocked));
            lblTotalRevenue.setText(revenue + " DT");
        } catch (SQLException e) {
            showError("Erreur statistiques rapides", e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Loyalty Stats
            Map<String, Integer> loyaltyStats = clientService.getStatsNiveau();
            ObservableList<PieChart.Data> loyaltyData = FXCollections.observableArrayList();
            loyaltyStats.forEach((niveau, count) -> {
                loyaltyData.add(new PieChart.Data(niveau + " (" + count + ")", count));
            });
            loyaltyPieChart.setData(loyaltyData);

            // Nationality Stats
            Map<String, Integer> nationalityStats = clientService.getStatsNationalite();
            ObservableList<PieChart.Data> nationalityData = FXCollections.observableArrayList();
            nationalityStats.forEach((nat, count) -> {
                nationalityData.add(new PieChart.Data(nat + " (" + count + ")", count));
            });
            nationalityPieChart.setData(nationalityData);

            updateQuickStats();
        } catch (SQLException e) {
            showError("Erreur de chargement des statistiques", e.getMessage());
        }
    }

    private void handleEditClient(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditClientDialog.fxml"));
            Parent root = loader.load();
            
            EditClientController controller = loader.getController();
            controller.setClient(client);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                clientService.update(client);
                refreshTable();
            }
        } catch (Exception e) {
            showError("Erreur de modification", e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchView(VBox view) {
        userManagementView.setVisible(false);
        statisticsView.setVisible(false);
        adminManagementView.setVisible(false);
        reservationsView.setVisible(false);
        accommodationsView.setVisible(false);
        activitiesView.setVisible(false);
        reviewsView.setVisible(false);
        
        view.setVisible(true);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            utils.SessionManager.cleanSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();
            mainStackPane.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void toggleSidebar() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        double targetWidth = isSidebarHidden ? SIDEBAR_WIDTH : 0;
        
        javafx.animation.KeyValue kvWidth = new javafx.animation.KeyValue(sidebarVBox.prefWidthProperty(), targetWidth);
        javafx.animation.KeyValue kvMinWidth = new javafx.animation.KeyValue(sidebarVBox.minWidthProperty(), targetWidth);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), kvWidth, kvMinWidth);
        
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(e -> {
            isSidebarHidden = !isSidebarHidden;
            if (isSidebarHidden) {
                sidebarVBox.setVisible(false);
                sidebarVBox.setManaged(false);
            }
        });

        if (!isSidebarHidden) { // Transitioning to hidden? Let's do nothing special.
            // But if we are showing it back, we must make it visible first.
        } else {
            sidebarVBox.setVisible(true);
            sidebarVBox.setManaged(true);
        }
        
        timeline.play();
    }
}
