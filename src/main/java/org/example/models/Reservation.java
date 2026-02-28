package org.example.models;

import java.time.LocalDateTime;

public class Reservation {

    private int idReservation;
    private int clientId;
    private LocalDateTime dateReservation;
    private String statut;
    private String modalitesPaiement;

    // NOUVEAU
    private String pays;
    private String ville;

    // =========================
    // GETTERS / SETTERS
    // =========================

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModalitesPaiement() { return modalitesPaiement; }
    public void setModalitesPaiement(String modalitesPaiement) { this.modalitesPaiement = modalitesPaiement; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", clientId=" + clientId +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                ", modalitesPaiement='" + modalitesPaiement + '\'' +
                ", pays='" + pays + '\'' +
                ", ville='" + ville + '\'' +
                '}';
    }
}