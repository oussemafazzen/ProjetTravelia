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
    @FXML private TableColumn<Client, Integer> colId;
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

    @FXML private TableView<Client> tableSecurity;
    @FXML private TableColumn<Client, String> colSecEmail;
    @FXML private TableColumn<Client, Integer> colSecAttempts;
    @FXML private TableColumn<Client, Object> colSecStatut;
    @FXML private TableColumn<Client, Void> colSecActions;

    @FXML private TextField txtSearch;
    @FXML private PieChart loyaltyPieChart;
    @FXML private PieChart nationalityPieChart;
    @FXML private StackPane mainStackPane;
    @FXML private VBox userManagementView;
    @FXML private VBox statisticsView;
    @FXML private VBox securityLogView;


    private ClientService clientService = new ClientService();
    private ObservableList<Client> clientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupAdminTable();
        refreshTable();
        setupSecurityTable();
        
        // Real-time search listener
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTable(newValue);
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
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

    private void setupSecurityTable() {
        colSecEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSecAttempts.setCellValueFactory(new PropertyValueFactory<>("failed_attempts"));
        colSecStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        addSecurityButtons();
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
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfirmDeleteDialog.fxml"));
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

    private void addSecurityButtons() {
        Callback<TableColumn<Client, Void>, TableCell<Client, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Client, Void> call(final TableColumn<Client, Void> param) {
                return new TableCell<>() {
                    private final Button btnReset = new Button("Réinit.");

                    {
                        btnReset.setOnAction((ActionEvent event) -> {
                            Client client = getTableView().getItems().get(getIndex());
                            try {
                                client.setFailed_attempts(0);
                                client.setStatut(Statut.ACTIF);
                                clientService.update(client);
                                refreshSecurityTable();
                                refreshTable(); // Sync main table too
                            } catch (SQLException e) {
                                showError("Erreur de réinitialisation", e.getMessage());
                            }
                        });
                        btnReset.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnReset);
                        }
                    }
                };
            }
        };
        colSecActions.setCellFactory(cellFactory);
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
    void showSecurityLog() {
        switchView(securityLogView);
        refreshSecurityTable();
    }


    private void refreshSecurityTable() {
        try {
            List<Client> riskyUsers = clientService.getAll().stream()
                    .filter(u -> u.getFailed_attempts() > 0)
                    .collect(Collectors.toList());
            tableSecurity.setItems(FXCollections.observableArrayList(riskyUsers));
        } catch (SQLException e) {
            showError("Erreur de journal de sécurité", e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditClientDialog.fxml"));
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
        securityLogView.setVisible(false);
        adminManagementView.setVisible(false);
        view.setVisible(true);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            utils.SessionManager.cleanSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
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
}
