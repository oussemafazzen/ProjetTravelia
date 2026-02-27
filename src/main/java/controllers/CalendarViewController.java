package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Billet;
import models.Reservation;
import services.ServiceBillet;
import services.ServiceReservation;
import utils.SessionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendarViewController {

    @FXML private DatePicker datePicker;
    @FXML private VBox detailsBox;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();

    private final Map<LocalDate, List<Reservation>> reservationsByDate = new HashMap<>();

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {

        // 1) Charger reservations (admin => tout, client => ses réservations)
        List<Reservation> reservations;

        if (SessionContext.isAdmin()) {
            reservations = safeList(sr.getAll());
        } else {
            Integer clientId = SessionContext.getCurrentUserId();
            if (clientId == null) {
                detailsBox.getChildren().setAll(makeMuted("Aucun utilisateur connecté."));
                datePicker.setDisable(true);
                return;
            }
            reservations = safeList(sr.getByClientId(clientId));
        }

        // 2) Construire cache LocalDate -> reservations
        reservationsByDate.clear();
        for (Reservation r : reservations) {
            LocalDate d = extractLocalDate(r);
            if (d == null) continue;
            reservationsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(r);
        }

        // 3) Marquer jours réservés
        installDateMarks();

        // 4) Valeur par défaut + listener
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        showDetailsForDate(today);

        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showDetailsForDate(newV);
        });
    }

    // ===================== DETAILS =====================

    private void showDetailsForDate(LocalDate date) {
        detailsBox.getChildren().clear();

        List<Reservation> list = reservationsByDate.getOrDefault(date, Collections.emptyList());

        Label header = new Label("Date : " + date.format(df) + " — Réservations : " + list.size());
        header.getStyleClass().add("card-title");
        detailsBox.getChildren().add(header);

        if (list.isEmpty()) {
            detailsBox.getChildren().add(makeMuted("Aucune réservation ce jour."));
            return;
        }

        for (Reservation r : list) {
            detailsBox.getChildren().add(buildReservationDetailsCard(r));
        }
    }

    private VBox buildReservationDetailsCard(Reservation r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("ticket-row"); // reuse ton style joli

        // Ligne top
        HBox top = new HBox(10);
        Label title = new Label("Réservation #" + r.getIdReservation());
        title.getStyleClass().add("section-mini");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label(safe(r.getStatut()));
        badge.getStyleClass().add(getBadgeClass(r.getStatut()));

        top.getChildren().addAll(title, sp, badge);

        // Paiement
        Label pay = new Label("Paiement: " + safe(r.getModalitesPaiement()));
        pay.getStyleClass().add("muted-small");

        card.getChildren().addAll(top, pay);

        // Billets liés
        List<Billet> billets = safeList(sb.getByReservationId(r.getIdReservation()));

        if (billets.isEmpty()) {
            card.getChildren().add(makeMutedSmall("Aucun billet associé."));
            return card;
        }

        double total = 0;
        for (Billet b : billets) {
            total += b.getPrix();
            Label line = new Label("• " + billetLine(b));
            line.getStyleClass().add("muted-small");
            card.getChildren().add(line);
        }

        Separator sep = new Separator();
        sep.setOpacity(0.6);

        HBox totalRow = new HBox(10);
        Label totalLbl = new Label("Total");
        totalLbl.getStyleClass().add("section-mini");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Label totalVal = new Label(String.format("%.0f DT", total));
        totalVal.getStyleClass().add("ticket-price");

        totalRow.getChildren().addAll(totalLbl, sp2, totalVal);

        card.getChildren().addAll(sep, totalRow);
        return card;
    }

    private String billetLine(Billet b) {
        String num = safe(b.getNumeroBillet());
        String type = safe(b.getTypeTransport());

        String dep = (b.getDateDepart() != null) ? b.getDateDepart().format(dtf) : "-";
        String arr = (b.getDateArrivee() != null) ? b.getDateArrivee().format(dtf) : "-";

        return num + " | " + type + " | " + dep + " → " + arr;
    }

    // ===================== MARK DAYS =====================

    private void installDateMarks() {
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setTooltip(null);
                    setStyle(null);
                    return;
                }

                List<Reservation> res = reservationsByDate.get(item);
                if (res != null && !res.isEmpty()) {
                    // highlight violet doux (match ton thème)
                    setStyle("-fx-background-color: rgba(142,45,226,0.18); -fx-background-radius: 10;");
                    setTooltip(new Tooltip(res.size() + " réservation(s)"));
                } else {
                    setTooltip(null);
                    setStyle(null);
                }
            }
        });
    }

    // ===================== ACTIONS =====================

    @FXML
    private void onClose() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    // ===================== HELPERS =====================

    // ✅ FIX: générique pour Reservation ET Billet
    private <T> List<T> safeList(List<T> list) {
        return (list == null) ? new ArrayList<>() : list;
    }

    private Label makeMuted(String txt) {
        Label l = new Label(txt);
        l.getStyleClass().add("muted");
        return l;
    }

    private Label makeMutedSmall(String txt) {
        Label l = new Label(txt);
        l.getStyleClass().add("muted-small");
        return l;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String getBadgeClass(String statut) {
        if (statut == null) return "badge-neutral";
        String s = statut.toLowerCase();
        if (s.contains("attente")) return "badge-waiting";
        if (s.contains("confirm")) return "badge-success";
        if (s.contains("annul")) return "badge-danger";
        return "badge-neutral";
    }

    // ✅ Fix LocalDateTime -> LocalDate
    private LocalDate extractLocalDate(Reservation r) {
        if (r == null || r.getDateReservation() == null) return null;

        // Ton modèle a LocalDateTime (selon l'erreur)
        // si un jour ça change, ça reste safe
        Object dtObj = r.getDateReservation();

        if (dtObj instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (dtObj instanceof LocalDate ld) return ld;

        if (dtObj instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }

        return null;
    }
}