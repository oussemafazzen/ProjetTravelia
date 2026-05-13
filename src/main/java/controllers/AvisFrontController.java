package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.Activite;
import models.Avis;
import models.Hebergement;
import models.PhotoAvis;
import services.ActiviteService;
import services.AvisService;
import services.HebergementService;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AvisFrontController {

    @FXML private Label lblAvgNote;
    @FXML private Label lblAvgStars;
    @FXML private Label lblTotalAvis;
    @FXML private Label lblTotalPhotos;
    @FXML private VBox reviewsContainer;

    @FXML private ProgressBar pb5, pb4, pb3, pb2, pb1;
    @FXML private Label lblCount5, lblCount4, lblCount3, lblCount2, lblCount1;

    private final AvisService avisService = new AvisService();
    private final HebergementService hebergementService = new HebergementService();
    private final ActiviteService activiteService = new ActiviteService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        loadAvisData();
    }

    private void loadAvisData() {
        try {
            List<Avis> listeAvis = avisService.getAll();
            
            // 1. Calculate Statistics
            int totalAvis = listeAvis.size();
            int totalPhotos = 0;
            double sumNotes = 0;
            int[] noteDistribution = new int[6]; // index 1 to 5

            for (Avis avis : listeAvis) {
                sumNotes += avis.getNote();
                noteDistribution[avis.getNote()]++;
                if (avis.getPhotos() != null) {
                    totalPhotos += avis.getPhotos().size();
                }
            }

            double avgNote = totalAvis > 0 ? sumNotes / totalAvis : 0;
            
            // Set Overview stats
            lblTotalAvis.setText(String.valueOf(totalAvis));
            lblTotalPhotos.setText(String.valueOf(totalPhotos));
            lblAvgNote.setText(String.format("%.1f/5", avgNote));
            lblAvgStars.setText(getStarString(Math.round(avgNote)));

            // Set Distribution stats
            if (totalAvis > 0) {
                updateDistributionRow(pb5, lblCount5, noteDistribution[5], totalAvis);
                updateDistributionRow(pb4, lblCount4, noteDistribution[4], totalAvis);
                updateDistributionRow(pb3, lblCount3, noteDistribution[3], totalAvis);
                updateDistributionRow(pb2, lblCount2, noteDistribution[2], totalAvis);
                updateDistributionRow(pb1, lblCount1, noteDistribution[1], totalAvis);
            }

            // 2. Render review cards
            reviewsContainer.getChildren().clear();
            for (Avis avis : listeAvis) {
                reviewsContainer.getChildren().add(createReviewCard(avis));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleNewAvis(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/AvisForm.fxml"));
            javafx.scene.Parent root = loader.load();
            
            AvisFormController controller = loader.getController();
            controller.setParentController(this);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Laisser un avis");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDistributionRow(ProgressBar pb, Label lbl, int count, int total) {
        pb.setProgress((double) count / total);
        lbl.setText(count + " avis");
        // Style adjustments based on Figma
        if (count > 0) {
            pb.setStyle("-fx-accent: #f59e0b;"); // orange
        } else {
            pb.setStyle("-fx-accent: #e2e8f0;"); // gray empty
        }
    }

    private String getStarString(long rating) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<rating; i++) sb.append("★");
        for(int i=(int)rating; i<5; i++) sb.append("☆");
        return sb.toString();
    }

    private VBox createReviewCard(Avis avis) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 24; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        // --- ROW 1: Header (Name, Tag, Rating) ---
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        String clientName = avis.getClient() != null ? avis.getClient().getPrenom() + " " + avis.getClient().getNom() : ("Client #" + avis.getIdClient());
        Label lblName = new Label(clientName);
        lblName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label lblTag = new Label(avis.getTypeService());
        lblTag.setStyle(getTagStyle(avis.getTypeService()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStars = new Label(getStarString(avis.getNote()));
        lblStars.setStyle("-fx-font-size: 16px; -fx-text-fill: #f59e0b; -fx-padding: 0 5 0 0;");
        Label lblNumericNote = new Label(avis.getNote() + "/5");
        lblNumericNote.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + getRatingColor(avis.getNote()) + ";");
        HBox ratingBox = new HBox(lblStars, lblNumericNote);
        ratingBox.setAlignment(Pos.CENTER_RIGHT);

        headerRow.getChildren().addAll(lblName, lblTag, spacer, ratingBox);

        // --- ROW 2: Date & Service ---
        HBox dateRow = new HBox(15);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        
        String dateStr = avis.getDatePublication() != null ? dateFormat.format(avis.getDatePublication()) : "N/A";
        Label lblDate = new Label("📅 " + dateStr);
        lblDate.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        
        // RESOLVE SERVICE NAME
        String serviceDisplayName = "Service #" + avis.getIdService();
        try {
            if ("Hébergement".equalsIgnoreCase(avis.getTypeService())) {
                Hebergement h = hebergementService.recupParIdHebergement(avis.getIdService());
                if (h != null) serviceDisplayName = h.getNom();
            } else if ("Activité".equalsIgnoreCase(avis.getTypeService())) {
                Activite a = activiteService.recupParIdActivite(avis.getIdService());
                if (a != null) serviceDisplayName = a.getNom();
            } else if ("Transport".equalsIgnoreCase(avis.getTypeService())) {
                // For transport, we can logic-out a name if we had a table, 
                // but since we mapped 1=Avion, 2=Bus etc in the form:
                if (avis.getIdService() == 1) serviceDisplayName = "Avion";
                else if (avis.getIdService() == 2) serviceDisplayName = "Bus";
                else if (avis.getIdService() == 3) serviceDisplayName = "Train";
            }
        } catch (SQLException e) { e.printStackTrace(); }

        String serviceIcon = getServiceIcon(avis.getTypeService());
        Label lblService = new Label(serviceIcon + " " + serviceDisplayName);
        lblService.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        dateRow.getChildren().addAll(lblDate, lblService);

        // --- ROW 3: Review Comment ---
        Label lblComment = new Label(avis.getCommentaire());
        lblComment.setWrapText(true);
        lblComment.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-padding: 10; -fx-background-color: #f8fafc; -fx-background-radius: 8;");
        lblComment.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(headerRow, dateRow, lblComment);

        // --- ROW 4: Photos (If any) ---
        int photoCount = avis.getPhotos() != null ? avis.getPhotos().size() : 0;
        if (photoCount > 0) {
            Label lblPhotoTitle = new Label("🖼️ Photos (" + photoCount + ")");
            lblPhotoTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
            
            HBox photosBox = new HBox(15);
            for (PhotoAvis photo : avis.getPhotos()) {
                VBox photoContainer = new VBox(8);
                photoContainer.setAlignment(Pos.CENTER);
                photoContainer.setPrefSize(250, 160);
                photoContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-overflow: hidden;");
                
                try {
                    String cleanPath = photo.getCheminFichier();
                    // Load actual image from file system OR resource
                    Image img;
                    if (cleanPath.startsWith("/images/")) {
                        // If it's in our newly copied project path
                        File f = new File("src/main/resources" + cleanPath);
                        if (f.exists()) {
                            img = new Image(f.toURI().toString());
                        } else {
                            // Try resource
                            img = new Image(getClass().getResource(cleanPath).toExternalForm());
                        }
                    } else {
                        // Fallback or absolute
                        img = new Image(new File(cleanPath).toURI().toString());
                    }

                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(240);
                    iv.setFitHeight(130);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-cursor: hand;");
                    
                    // Photo Zoom Functionality
                    iv.setOnMouseClicked(e -> showZoomedImage(img));

                    Label legend = new Label(photo.getLegende());
                    legend.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                    
                    photoContainer.getChildren().addAll(iv, legend);
                } catch (Exception e) {
                    // Fallback to placeholder if image fails to load
                    Label icon = new Label("⚠️ Photo indisponible");
                    icon.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
                    photoContainer.getChildren().add(icon);
                }
                
                photosBox.getChildren().add(photoContainer);
            }
            card.getChildren().addAll(lblPhotoTitle, photosBox);
        }

        // --- ROW 5: Actions Bottom ---
        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));
        actionsRow.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");

        Label btnUtile = new Label("👍 Utile");
        btnUtile.setStyle("-fx-font-size: 13px; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-font-weight: bold;");
        Label btnRepondre = new Label("💬 Répondre");
        btnRepondre.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold;");

        Region spacerBottom = new Region();
        HBox.setHgrow(spacerBottom, Priority.ALWAYS);

        Label lblPhotoStatus = new Label("🖼️ " + photoCount + " photo(s)");
        lblPhotoStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        actionsRow.getChildren().addAll(btnUtile, btnRepondre, spacerBottom, lblPhotoStatus);
        card.getChildren().add(actionsRow);

        return card;
    }

    private String getTagStyle(String type) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12; ";
        if (type == null) type = "";
        
        switch (type.toLowerCase()) {
            case "hebergement":
            case "hébergement":
                return base + "-fx-background-color: #ffedd5; -fx-text-fill: #ea580c;"; // Orange
            case "activite":
            case "activité":
                return base + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"; // Green
            case "transport":
                return base + "-fx-background-color: #f3e8ff; -fx-text-fill: #9333ea;"; // Purple
            default:
                return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;"; // Gray
        }
    }

    private String getRatingColor(int note) {
        if (note >= 4) return "#16a34a"; // Green
        if (note == 3) return "#f59e0b"; // Orange/Yellow
        return "#ef4444"; // Red for 1 or 2
    }

    private String getServiceIcon(String type) {
        if (type == null) type = "";
        switch (type.toLowerCase()) {
            case "hebergement": case "hébergement": return "🏢";
            case "activite": case "activité": return "🏖️";
            case "transport": return "🚌";
            default: return "📌";
        }
    }

    private void showZoomedImage(Image img) {
        javafx.stage.Stage zoomStage = new javafx.stage.Stage();
        zoomStage.setTitle("Aperçu Photo");
        zoomStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        ImageView zoomIv = new ImageView(img);
        zoomIv.setPreserveRatio(true);
        zoomIv.setFitWidth(800);
        zoomIv.setFitHeight(600);
        
        StackPane root = new StackPane(zoomIv);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 20;");
        
        zoomStage.setScene(new javafx.scene.Scene(root));
        zoomStage.show();
    }

}
