package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Activite;
import models.ActivityRecommendation;
import models.InscriptionActivite;
import services.ActiviteService;
import services.ActivityRecommendationService;
import services.GeminiChatService;
import services.InscriptionActiviteService;
import utils.ChatbotSessionManager;
import utils.SessionContext;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ActiviteFrontController implements Initializable {

    @FXML private Label lblTotalActivites;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterCategorie;
    @FXML private FlowPane cardsContainer;
    @FXML private VBox boxRecommendations;

    @FXML private HBox paginationBox;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private javafx.scene.text.Text txtCurrentPage;
    @FXML private javafx.scene.text.Text txtTotalPages;

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 6;
    private List<Activite> currentFilteredList = new ArrayList<>();

    private ActiviteService activiteService = new ActiviteService();
    private InscriptionActiviteService inscriptionService = new InscriptionActiviteService();
    private final ActivityRecommendationService recoService = new ActivityRecommendationService();

    private List<Activite> allActivites = new ArrayList<>();
    private Map<Integer, List<InscriptionActivite>> inscriptionsByActivite = new HashMap<>();
    private Map<Integer, Integer> participantsByActivite = new HashMap<>();
    private int currentClientId = -1;

    // ─── Chatbot fields ───
    private final GeminiChatService geminiService = new GeminiChatService();
    private final ChatbotSessionManager sessionManager = new ChatbotSessionManager();
    private VBox chatPanel;
    private VBox chatMessagesContainer;
    private ScrollPane chatScrollPane;
    private TextField chatInput;
    private Button chatFab; // Floating action button
    private final javafx.beans.property.BooleanProperty chatOpen = new javafx.beans.property.SimpleBooleanProperty(false);

    // Onboarding state
    private static final String[] ONBOARDING_QUESTIONS = {
            "Bienvenue sur Travelia ! \uD83C\uDF1F Quel type d'activité vous intéresse le plus ? (Culturel, Aventure, Sport, Gastronomie, Nature, Détente)",
            "Super ! Quel est votre budget approximatif par personne ? (ex: 30 DT, 80 DT, 150 DT)",
            "Enfin, préférez-vous des activités en groupe ou plutôt des expériences privées ?"
    };
    private int onboardingStep = 0;
    private Map<String, String> onboardingAnswers = new LinkedHashMap<>();
    private boolean onboardingDone = false;

    // Category badge colors
    private static final Map<String, String> CATEGORY_COLORS = new LinkedHashMap<>();
    static {
        CATEGORY_COLORS.put("Culturel", "#0156ff");
        CATEGORY_COLORS.put("Aventure", "#22c55e");
        CATEGORY_COLORS.put("Gastronomie", "#f59e0b");
        CATEGORY_COLORS.put("Nature", "#10b981");
        CATEGORY_COLORS.put("Sport", "#ef4444");
        CATEGORY_COLORS.put("Détente", "#8b5cf6");
        CATEGORY_COLORS.put("Découverte", "#06b6d4");
        CATEGORY_COLORS.put("Culture", "#0156ff");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get current logged-in client
        if (SessionContext.getCurrentUserId() != null) {
            currentClientId = SessionContext.getCurrentUserId();
        }

        loadData();

        // Load AI recommendations
        if (currentClientId > 0) {
            loadRecommendations(currentClientId);
        }

        // Search filter
        tfSearch.textProperty().addListener((obs, o, n) -> filterCards());

        // Category filter
        cbFilterCategorie.setOnAction(e -> filterCards());

        // Initialize chatbot
        initChatbot();
    }

    public void loadData() {
        try {
            allActivites = activiteService.recupToutesActivites();
            Collections.shuffle(allActivites); // Randomize display order
            List<InscriptionActivite> allInscriptions = inscriptionService.recupToutesInscriptions();

            // Group inscriptions by activite
            inscriptionsByActivite.clear();
            participantsByActivite.clear();
            for (InscriptionActivite insc : allInscriptions) {
                inscriptionsByActivite.computeIfAbsent(insc.getIdActivite(), k -> new ArrayList<>()).add(insc);
                participantsByActivite.merge(insc.getIdActivite(), insc.getNombreParticipants(), (a, b) -> a + b);
            }

            lblTotalActivites.setText(String.valueOf(allActivites.size()));
            
            // Set initial filtered list so pagination calculates properly on start
            currentFilteredList = allActivites;

            // Populate category filter
            Set<String> categories = allActivites.stream()
                    .map(Activite::getCategorie)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
            List<String> catList = new ArrayList<>();
            catList.add("Toutes les catégories");
            catList.addAll(categories);
            cbFilterCategorie.setItems(FXCollections.observableArrayList(catList));
            cbFilterCategorie.setValue("Toutes les catégories");

            buildCards(allActivites);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterCards() {
        String search = tfSearch.getText() != null ? tfSearch.getText().toLowerCase() : "";
        String category = cbFilterCategorie.getValue();

        currentFilteredList = allActivites.stream()
                .filter(a -> {
                    boolean matchSearch = search.isEmpty() ||
                            a.getNom().toLowerCase().contains(search) ||
                            a.getLieu().toLowerCase().contains(search) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(search));
                    boolean matchCategory = category == null || category.equals("Toutes les catégories") ||
                            category.equals(a.getCategorie());
                    return matchSearch && matchCategory;
                })
                .collect(Collectors.toList());

        currentPage = 0;
        buildCards(currentFilteredList);
    }

    private void buildCards(List<Activite> activites) {
        cardsContainer.getChildren().clear();

        if (activites == null || activites.isEmpty()) {
            Label emptyLabel = new Label("Aucune activité trouvée.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            cardsContainer.getChildren().add(emptyLabel);
            updatePaginationUI();
            return;
        }

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, activites.size());

        List<Activite> pageItems = activites.subList(start, end);

        for (Activite activite : pageItems) {
            VBox card = createActivityCard(activite);
            cardsContainer.getChildren().add(card);
        }

        updatePaginationUI();
    }

    private void updatePaginationUI() {
        if (currentFilteredList == null || btnPrev == null) return;
        
        int total = currentFilteredList.size();
        int totalPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        txtCurrentPage.setText(String.valueOf(currentPage + 1));
        txtTotalPages.setText(String.valueOf(totalPages));
        
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= totalPages - 1);
        
        // Hide pagination box if total items fit in one page or list is empty
        if (paginationBox != null) {
            paginationBox.setVisible(total > ITEMS_PER_PAGE);
            paginationBox.setManaged(total > ITEMS_PER_PAGE);
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentFilteredList == null) return;
        int totalPages = (int) Math.ceil((double) currentFilteredList.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            buildCards(currentFilteredList);
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            buildCards(currentFilteredList);
        }
    }

    private VBox createActivityCard(Activite activite) {
        VBox card = new VBox(0);
        card.setPrefWidth(460);
        card.setMaxWidth(460);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 16;");

        // --- Image area with category badge ---
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(180);
        imageArea.setMinHeight(180);
        imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3a5f, #2d5a87); " +
                "-fx-background-radius: 16 16 0 0;");

        // Category badge
        String catColor = CATEGORY_COLORS.getOrDefault(activite.getCategorie(), "#64748b");
        Label badge = new Label(activite.getCategorie() != null ? activite.getCategorie() : "Autre");
        badge.setStyle("-fx-background-color: " + catColor + "; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12; " +
                "-fx-background-radius: 8;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(12, 12, 0, 0));

        // Activity icon in center
        Label iconLabel = new Label("🏔");
        iconLabel.setStyle("-fx-font-size: 48px; -fx-opacity: 0.4;");
        StackPane.setAlignment(iconLabel, Pos.CENTER);

        imageArea.getChildren().addAll(iconLabel, badge);

        // --- Card Body ---
        VBox body = new VBox(10);
        body.setPadding(new Insets(18, 20, 18, 20));

        // Title
        Label title = new Label(activite.getNom());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        // Location
        Label location = new Label("📍 " + activite.getLieu());
        location.setStyle("-fx-font-size: 13px; -fx-text-fill: #0156ff;");

        // Description
        Label description = new Label(activite.getDescription() != null ? activite.getDescription() : "");
        description.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #64748b;");
        description.setWrapText(true);
        description.setMaxHeight(40);

        // Info row: Duration + Max capacity
        HBox infoRow = new HBox(25);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label duration = new Label("⏱ " + activite.getDuree() + "h");
        duration.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: bold;");

        Label maxCapLabel = new Label("👥 Max " + activite.getCapaciteMax() + " pers.");
        maxCapLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        infoRow.getChildren().addAll(duration, maxCapLabel);

        // Price + inscriptions row
        HBox priceRow = new HBox(25);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label("💰 " + String.format("%.0f", activite.getPrix()) + " DT/pers.");
        price.setStyle("-fx-font-size: 14px; -fx-text-fill: #0156ff; -fx-font-weight: bold;");

        int totalInscr = inscriptionsByActivite.containsKey(activite.getIdActivite()) ?
                inscriptionsByActivite.get(activite.getIdActivite()).size() : 0;
        Label inscrLabel = new Label("🏷 " + totalInscr + " inscrit(s)");
        inscrLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        priceRow.getChildren().addAll(price, inscrLabel);

        // --- Current client's inscription section (only their own) ---
        VBox inscriptionsSection = new VBox(6);
        List<InscriptionActivite> allInscriptions = inscriptionsByActivite.getOrDefault(activite.getIdActivite(), Collections.emptyList());

        // Filter: only show the current client's inscriptions
        List<InscriptionActivite> myInscriptions = allInscriptions.stream()
                .filter(insc -> insc.getIdClient() == currentClientId)
                .collect(Collectors.toList());

        if (!myInscriptions.isEmpty()) {
            Label inscrTitle = new Label("Mon Inscription");
            inscrTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            inscriptionsSection.getChildren().add(inscrTitle);

            for (InscriptionActivite insc : myInscriptions) {
                HBox inscRow = new HBox(10);
                inscRow.setAlignment(Pos.CENTER_LEFT);
                inscRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-padding: 8 12;");

                VBox inscInfo = new VBox(2);
                Label dateLabel = new Label(insc.getDateActivite() != null ?
                        insc.getDateActivite().toString() + " • " + insc.getNombreParticipants() + " participant(s)" : "");
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                inscInfo.getChildren().add(dateLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Status badge
                String statusColor;
                String statut = insc.getStatut() != null ? insc.getStatut() : "";
                switch (statut) {
                    case "Confirmée": statusColor = "#22c55e"; break;
                    case "Annulée": statusColor = "#ef4444"; break;
                    default: statusColor = "#f59e0b"; break;
                }
                Label statusBadge = new Label(statut);
                statusBadge.setStyle("-fx-background-color: " + statusColor + "22; -fx-text-fill: " + statusColor +
                        "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 6;");

                inscRow.getChildren().addAll(inscInfo, spacer, statusBadge);
                inscriptionsSection.getChildren().add(inscRow);
            }
        }

        // --- Availability bar ---
        int totalParticipants = participantsByActivite.getOrDefault(activite.getIdActivite(), 0);
        int maxCap = activite.getCapaciteMax();
        double availPercent = maxCap > 0 ? (double) totalParticipants / maxCap : 0;

        VBox availSection = new VBox(4);
        HBox availHeader = new HBox();
        availHeader.setAlignment(Pos.CENTER_LEFT);
        Label availLabel = new Label("Disponibilité");
        availLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Region avSpacer = new Region();
        HBox.setHgrow(avSpacer, Priority.ALWAYS);
        Label availValue = new Label(totalParticipants + "/" + maxCap + " places");
        availValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        availHeader.getChildren().addAll(availLabel, avSpacer, availValue);

        ProgressBar availBar = new ProgressBar(Math.min(availPercent, 1.0));
        availBar.setMaxWidth(Double.MAX_VALUE);
        availBar.setPrefHeight(8);
        String barColor = availPercent >= 1.0 ? "#ef4444" : availPercent >= 0.7 ? "#f59e0b" : "#22c55e";
        availBar.setStyle("-fx-accent: " + barColor + ";");

        availSection.getChildren().addAll(availHeader, availBar);

        // --- Action buttons: Modifier + S'inscrire ---
        HBox buttonsRow = new HBox(12);
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.setPadding(new Insets(4, 0, 0, 0));

        // Modifier button (only visible if the client has an inscription)
        Button btnModifier = new Button("Modifier");
        btnModifier.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnModifier, Priority.ALWAYS);
        btnModifier.setStyle("-fx-background-color: white; -fx-text-fill: #334155; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 0; " +
                "-fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand;");

        if (!myInscriptions.isEmpty()) {
            // Allow editing the client's own inscription
            InscriptionActivite myInsc = myInscriptions.get(0);
            btnModifier.setOnAction(e -> openInscriptionForm(myInsc, activite));
        } else {
            btnModifier.setDisable(true);
            btnModifier.setStyle(btnModifier.getStyle().replace("-fx-cursor: hand;", "-fx-cursor: default; -fx-opacity: 0.5;"));
        }

        // S'inscrire button
        Button btnInscrire = new Button("S'inscrire");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnInscrire, Priority.ALWAYS);
        btnInscrire.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 10 0; -fx-background-radius: 10; -fx-cursor: hand;");

        boolean isFull = totalParticipants >= maxCap;
        boolean alreadyInscribed = !myInscriptions.isEmpty();

        if (isFull) {
            btnInscrire.setText("Complet");
            btnInscrire.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #94a3b8; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 0; " +
                    "-fx-background-radius: 10; -fx-cursor: default;");
            btnInscrire.setDisable(true);
        } else if (alreadyInscribed) {
            btnInscrire.setText("Déjà inscrit");
            btnInscrire.setStyle("-fx-background-color: #0156ff; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 0; " +
                    "-fx-background-radius: 10; -fx-cursor: default;");
            btnInscrire.setDisable(true);
        } else {
            btnInscrire.setOnAction(e -> openInscriptionForm(null, activite));
        }

        buttonsRow.getChildren().addAll(btnModifier, btnInscrire);

        // Hover effect on card
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4)",
                "dropshadow(gaussian, rgba(1,86,255,0.15), 18, 0, 0, 6)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian, rgba(1,86,255,0.15), 18, 0, 0, 6)",
                "dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4)")));

        body.getChildren().addAll(title, location, description, infoRow, priceRow);

        if (!myInscriptions.isEmpty()) {
            Separator sep1 = new Separator();
            sep1.setStyle("-fx-padding: 4 0 0 0;");
            body.getChildren().addAll(sep1, inscriptionsSection);
        }

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-padding: 4 0 0 0;");
        body.getChildren().addAll(sep2, availSection, buttonsRow);

        card.getChildren().addAll(imageArea, body);
        return card;
    }

    private void openInscriptionForm(InscriptionActivite existing, Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InscriptionActiviteForm.fxml"));
            Parent root = loader.load();

            InscriptionActiviteFormController formCtrl = loader.getController();
            formCtrl.setParentController(this);

            if (existing != null) {
                // Edit mode
                formCtrl.setInscription(existing);
            } else {
                // New inscription: pre-fill the current client ID and activite ID
                InscriptionActivite newInsc = new InscriptionActivite();
                newInsc.setIdClient(currentClientId);
                newInsc.setIdActivite(activite.getIdActivite());
                formCtrl.setInscription(null);
                formCtrl.prefillClientAndActivite(currentClientId, activite.getIdActivite());
            }

            Stage stage = new Stage();
            stage.setTitle(existing != null ? "Modifier mon inscription" : "S'inscrire à " + activite.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh after modal closes
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        }
    }

    // =================== AI RECOMMENDATIONS ===================

    private void loadRecommendations(int clientId) {
        if (boxRecommendations == null) return;

        boxRecommendations.setVisible(true);
        boxRecommendations.setManaged(true);
        boxRecommendations.getChildren().clear();

        // Main wrapper card
        VBox mainCard = new VBox(15);
        mainCard.setStyle("-fx-background-color: white; -fx-padding: 25; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                "-fx-background-radius: 12;");

        // Header: Title + Refresh button
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("✨");
        iconLbl.setStyle("-fx-font-size: 18px;");
        Label titleLbl = new Label("Recommandations pour vous");
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1e293b;");
        titleBox.getChildren().addAll(iconLbl, titleLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblAiBadge = new Label("IA");
        lblAiBadge.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #a855f7); " +
                "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-padding: 3 8; -fx-background-radius: 6;");

        Region spacer2 = new Region();
        spacer2.setPrefWidth(8);

        Label lblRefresh = new Label("Rafraîchir");
        lblRefresh.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 13px; -fx-cursor: hand; " +
                "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-color: #f1f5f9; " +
                "-fx-background-radius: 6;");
        lblRefresh.setOnMouseEntered(e -> lblRefresh.setStyle(
                "-fx-text-fill: #4f46e5; -fx-background-color: #e2e8f0; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 13px;"));
        lblRefresh.setOnMouseExited(e -> lblRefresh.setStyle(
                "-fx-text-fill: #6366f1; -fx-background-color: #f1f5f9; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 13px;"));

        lblRefresh.setOnMouseClicked(e -> {
            lblRefresh.setText("Mise à jour...");
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), boxRecommendations);
            fade.setFromValue(1.0);
            fade.setToValue(0.2);
            fade.setOnFinished(ev -> {
                loadRecommendations(clientId);
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), boxRecommendations);
                fadeIn.setFromValue(0.2);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fade.play();
        });

        headerRow.getChildren().addAll(titleBox, spacer, lblAiBadge, spacer2, lblRefresh);
        mainCard.getChildren().add(headerRow);

        // Get recommendations from AI
        List<ActivityRecommendation> recos = recoService.recommendForClient(clientId, 3);

        if (recos == null || recos.isEmpty()) {
            Label noRec = new Label("Aucune suggestion pour le moment. Essayez de rafraîchir !");
            noRec.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 10 0;");
            mainCard.getChildren().add(noRec);
        } else {
            // Subtitle
            Label subtitle = new Label("Basé sur votre historique d'inscriptions (IA RandomForest).");
            subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            VBox.setMargin(subtitle, new Insets(-10, 0, 5, 0));
            mainCard.getChildren().add(subtitle);

            // Activity recommendation rows
            VBox rowsContainer = new VBox(10);
            for (ActivityRecommendation r : recos) {
                models.Activite act = r.getActivite();

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 14; -fx-cursor: hand; -fx-background-radius: 10; " +
                        "-fx-background-color: #f8fafc;");
                row.setOnMouseEntered(ev -> row.setStyle(
                        "-fx-padding: 14; -fx-cursor: hand; -fx-background-radius: 10; " +
                        "-fx-background-color: #f1f5f9;"));
                row.setOnMouseExited(ev -> row.setStyle(
                        "-fx-padding: 14; -fx-cursor: hand; -fx-background-radius: 10; " +
                        "-fx-background-color: #f8fafc;"));

                // Category color dot
                String catColor = CATEGORY_COLORS.getOrDefault(act.getCategorie(), "#64748b");
                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill: " + catColor + "; -fx-font-size: 20px;");

                // Info column
                VBox leftCol = new VBox(4);
                Label rowTitle = new Label(act.getNom());
                rowTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #334155;");
                rowTitle.setMaxWidth(500);
                rowTitle.setWrapText(true);

                HBox metaRow = new HBox(10);
                metaRow.setAlignment(Pos.CENTER_LEFT);

                Label catBadge = new Label(act.getCategorie() != null ? act.getCategorie() : "Autre");
                catBadge.setStyle("-fx-background-color: " + catColor + "22; -fx-text-fill: " + catColor +
                        "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6;");

                Label locLabel = new Label("📍 " + act.getLieu());
                locLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

                Label priceLabel = new Label(String.format("%.0f DT", act.getPrix()));
                priceLabel.setStyle("-fx-text-fill: #0156ff; -fx-font-size: 11px; -fx-font-weight: bold;");

                metaRow.getChildren().addAll(catBadge, locLabel, priceLabel);

                Label rowReason = new Label(r.getReason());
                rowReason.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

                leftCol.getChildren().addAll(rowTitle, metaRow, rowReason);

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                // Score badge
                Label scoreBadge = new Label(String.format("%.0f%%", r.getScore()));
                scoreBadge.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #a855f7); " +
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                        "-fx-padding: 4 12; -fx-background-radius: 8;");

                row.getChildren().addAll(dot, leftCol, rowSpacer, scoreBadge);

                // Click → scroll to the activity card or open inscription
                row.setOnMouseClicked(ev -> {
                    // Filter to show just this category
                    cbFilterCategorie.setValue(act.getCategorie());
                    filterCards();
                });

                rowsContainer.getChildren().add(row);
            }
            mainCard.getChildren().add(rowsContainer);
        }

        boxRecommendations.getChildren().add(mainCard);
    }

    // ═══════════════════════════════════════════════════════════════
    // ░░░ CHATBOT UI ░░░
    // ═══════════════════════════════════════════════════════════════

    private void initChatbot() {
        if (currentClientId <= 0) return; // Only for logged-in clients

        // Delay to ensure scene is loaded
        Platform.runLater(() -> {
            if (cardsContainer.getScene() == null) return;

            // 1. Get the top-level Dashboard AnchorPane so the chat floats above EVERYTHING
            javafx.scene.layout.AnchorPane windowRoot = null;
            if (cardsContainer.getScene().getRoot() instanceof javafx.scene.layout.AnchorPane) {
                windowRoot = (javafx.scene.layout.AnchorPane) cardsContainer.getScene().getRoot();
            } else {
                System.err.println("Chatbot: Could not find AnchorPane root in Dashboard");
                return;
            }

            // 2. Find the invisible "activitiesView" wrapper controlled by Dashboard
            javafx.scene.Node activitiesViewParent = cardsContainer;
            while (activitiesViewParent != null && !"activitiesView".equals(activitiesViewParent.getId())) {
                activitiesViewParent = activitiesViewParent.getParent();
            }
            if (activitiesViewParent == null) {
                System.err.println("Chatbot: Could not find activitiesView wrapper in tree");
                return;
            }

            buildChatFab(windowRoot, activitiesViewParent.visibleProperty());
            buildChatPanel(windowRoot, activitiesViewParent.visibleProperty());

            // Feed real activities from database into chatbot
            StringBuilder catalog = new StringBuilder();
            for (Activite act : allActivites) {
                catalog.append("- Nom: \"").append(act.getNom()).append("\"")
                       .append(" | Categorie: ").append(act.getCategorie())
                       .append(" | Prix: ").append(String.format("%.0f", act.getPrix())).append(" DT")
                       .append(" | Lieu: ").append(act.getLieu())
                       .append("\n");
            }
            geminiService.setAvailableActivities(catalog.toString());

            // Check if onboarding is needed
            onboardingDone = sessionManager.hasCompletedOnboarding(currentClientId);

            if (onboardingDone) {
                // Returning user: inject context and keep chat hidden
                Map<String, String> answers = sessionManager.getOnboardingAnswers(currentClientId);
                geminiService.injectOnboardingContext(answers);
            } else {
                // First visit: auto-open chat with onboarding
                toggleChat(true);
                onboardingStep = 0;
                addBotMessage(ONBOARDING_QUESTIONS[0]);
            }
        });
    }

    private void buildChatFab(javafx.scene.layout.AnchorPane root, javafx.beans.value.ObservableBooleanValue parentVisible) {
        chatFab = new Button("\uD83E\uDD16");
        chatFab.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #6366f1, #8b5cf6); " +
                "-fx-font-size: 26px; -fx-padding: 0; " +
                "-fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60; " +
                "-fx-background-radius: 30; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.5), 15, 0, 0, 5);"
        );
        chatFab.setOnAction(e -> toggleChat(!chatOpen.get()));
        
        javafx.scene.layout.AnchorPane.setBottomAnchor(chatFab, 30.0);
        javafx.scene.layout.AnchorPane.setRightAnchor(chatFab, 30.0);
        
        // Hide FAB if on another tab OR if chat is open
        chatFab.visibleProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
            () -> parentVisible.get() && !chatOpen.get(),
            parentVisible, chatOpen
        ));
        
        root.getChildren().add(chatFab);
    }

    private void buildChatPanel(javafx.scene.layout.AnchorPane root, javafx.beans.value.ObservableBooleanValue parentVisible) {
        chatPanel = new VBox();
        chatPanel.setPrefSize(380, 500);
        chatPanel.setMaxSize(380, 500);
        chatPanel.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8); " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 16;"
        );
        
        javafx.scene.layout.AnchorPane.setBottomAnchor(chatPanel, 100.0);
        javafx.scene.layout.AnchorPane.setRightAnchor(chatPanel, 30.0);
        
        // Show panel if on activities tab AND chat is open
        chatPanel.visibleProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
            () -> parentVisible.get() && chatOpen.get(),
            parentVisible, chatOpen
        ));

        // ─── Header ───
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                "-fx-background-radius: 16 16 0 0;"
        );

        // Bot avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(36, 36);
        avatar.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 18;");
        Label avatarIcon = new Label("\uD83E\uDD16");
        avatarIcon.setStyle("-fx-font-size: 18px;");
        avatar.getChildren().add(avatarIcon);

        VBox headerText = new VBox(2);
        Label headerTitle = new Label("Assistant Travelia");
        headerTitle.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 15px;");
        Label headerSub = new Label("En ligne");
        headerSub.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        headerText.getChildren().addAll(headerTitle, headerSub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("X");
        closeBtn.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0; " +
                "-fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30; " +
                "-fx-background-radius: 15; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.4), 6, 0, 0, 2);"
        );
        closeBtn.setOnAction(e -> toggleChat(false));

        header.getChildren().addAll(avatar, headerText, headerSpacer, closeBtn);

        // ─── Messages area ───
        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(15));
        chatMessagesContainer.setStyle("-fx-background-color: #f8fafc;");

        chatScrollPane = new ScrollPane(chatMessagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // ─── Input area ───
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(12, 14, 14, 14));
        inputArea.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        chatInput = new TextField();
        chatInput.setPromptText("Tapez votre message...");
        chatInput.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 20; -fx-background-radius: 20; " +
                "-fx-padding: 10 16; -fx-font-size: 13px;"
        );
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        chatInput.setOnAction(e -> handleChatSend());

        Button sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 0; " +
                "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; " +
                "-fx-background-radius: 20; -fx-cursor: hand;"
        );
        sendBtn.setOnAction(e -> handleChatSend());

        inputArea.getChildren().addAll(chatInput, sendBtn);

        chatPanel.getChildren().addAll(header, chatScrollPane, inputArea);
        root.getChildren().add(chatPanel);
    }

    private void toggleChat(boolean open) {
        chatOpen.set(open);
        
        if (open) {
            Platform.runLater(() -> {
                chatScrollPane.setVvalue(1.0);
                chatInput.requestFocus();
            });
        }
    }

    private void handleChatSend() {
        String text = chatInput.getText();
        if (text == null || text.trim().isEmpty()) return;
        chatInput.clear();

        // Add user message bubble
        addUserMessage(text.trim());

        if (!onboardingDone) {
            // We're in onboarding mode
            handleOnboardingAnswer(text.trim());
        } else {
            // Free chat mode — send to Gemini in background
            sendToGemini(text.trim());
        }
    }

    private void handleOnboardingAnswer(String answer) {
        // Save the answer
        String questionKey;
        switch (onboardingStep) {
            case 0: questionKey = "Type d'activité préféré"; break;
            case 1: questionKey = "Budget approximatif"; break;
            case 2: questionKey = "Préférence groupe/privé"; break;
            default: questionKey = "Question " + (onboardingStep + 1); break;
        }
        onboardingAnswers.put(questionKey, answer);
        onboardingStep++;

        if (onboardingStep < ONBOARDING_QUESTIONS.length) {
            // Ask next question
            addBotMessage(ONBOARDING_QUESTIONS[onboardingStep]);
        } else {
            // Onboarding done!
            onboardingDone = true;
            sessionManager.saveOnboardingAnswers(currentClientId, onboardingAnswers);
            geminiService.injectOnboardingContext(onboardingAnswers);

            addBotMessage("Merci pour vos réponses ! \uD83C\uDF89 Je connais maintenant vos préférences. " +
                    "Vous pouvez me poser n'importe quelle question sur nos activités !");
        }
    }

    private void sendToGemini(String userMessage) {
        // Show typing indicator
        HBox typingBubble = createTypingIndicator();
        chatMessagesContainer.getChildren().add(typingBubble);
        scrollToBottom();

        // Background thread for API call
        new Thread(() -> {
            String reply = geminiService.sendMessage(userMessage);
            Platform.runLater(() -> {
                chatMessagesContainer.getChildren().remove(typingBubble);
                addBotMessage(reply);
            });
        }).start();
    }

    private void addBotMessage(String text) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(0, 40, 0, 0));

        HBox bubble = new HBox(8);
        bubble.setAlignment(Pos.TOP_LEFT);

        // Small bot icon
        Label icon = new Label("\uD83E\uDD16");
        icon.setStyle("-fx-font-size: 14px; -fx-padding: 2 0 0 0;");

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setMaxWidth(260);
        msg.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14 14 14 4; " +
                "-fx-padding: 10 14; -fx-font-size: 13px; -fx-text-fill: #334155; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 2);"
        );

        bubble.getChildren().addAll(icon, msg);
        wrapper.getChildren().add(bubble);
        chatMessagesContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.setPadding(new Insets(0, 0, 0, 40));

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setMaxWidth(260);
        msg.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                "-fx-background-radius: 14 14 4 14; " +
                "-fx-padding: 10 14; -fx-font-size: 13px; -fx-text-fill: white;"
        );

        wrapper.getChildren().add(msg);
        chatMessagesContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private HBox createTypingIndicator() {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);

        Label dots = new Label("\uD83E\uDD16 En train d'écrire...");
        dots.setStyle(
                "-fx-background-color: #e2e8f0; -fx-background-radius: 14; " +
                "-fx-padding: 10 14; -fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-style: italic;"
        );

        wrapper.getChildren().add(dots);
        return wrapper;
    }

    private void scrollToBottom() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
}
