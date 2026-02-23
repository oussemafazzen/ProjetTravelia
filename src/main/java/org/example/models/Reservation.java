package org.example.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reservation {

    private int idReservation;
    private LocalDateTime dateReservation;
    private String statut;
    private String modalitesPaiement;
    private int idClient;
    private List<Billet> billets;

    public Reservation() {
        this.billets = new ArrayList<>();
    }

    public Reservation(LocalDateTime dateReservation, String statut,
                       String modalitesPaiement, int idClient) {
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.modalitesPaiement = modalitesPaiement;
        this.idClient = idClient;
        this.billets = new ArrayList<>();
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getModalitesPaiement() {
        return modalitesPaiement;
    }

    public void setModalitesPaiement(String modalitesPaiement) {
        this.modalitesPaiement = modalitesPaiement;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public List<Billet> getBillets() {
        return billets;
    }

    public void setBillets(List<Billet> billets) {
        this.billets = billets;
    }

    public void addBillet(Billet billet) {
        this.billets.add(billet);
        billet.setReservation(this);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                ", modalitesPaiement='" + modalitesPaiement + '\'' +
                ", idClient=" + idClient +
                ", billets=" + billets.size() +
                "}";
    }
}
