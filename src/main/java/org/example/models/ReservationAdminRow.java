package org.example.models;

import java.time.LocalDate;

public class ReservationAdminRow {

    private int idReservation;
    private LocalDate dateReservation;
    private String statut;
    private String modalitesPaiement;

    private int idClient;
    private String nomClient;
    private String prenomClient;

    // getters/setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public LocalDate getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModalitesPaiement() { return modalitesPaiement; }
    public void setModalitesPaiement(String modalitesPaiement) { this.modalitesPaiement = modalitesPaiement; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getPrenomClient() { return prenomClient; }
    public void setPrenomClient(String prenomClient) { this.prenomClient = prenomClient; }

    public String getClientFullName() {
        String n = (nomClient == null) ? "" : nomClient;
        String p = (prenomClient == null) ? "" : prenomClient;
        return (n + " " + p).trim();
    }
}