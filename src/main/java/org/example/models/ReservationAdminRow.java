package org.example.models;

public class ReservationAdminRow {

    private int idReservation;
    private String clientFullName;
    private String dateReservation;
    private String statut;
    private String modalitesPaiement;
    private double montantTotal;

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public String getClientFullName() { return clientFullName; }
    public void setClientFullName(String clientFullName) { this.clientFullName = clientFullName; }

    public String getDateReservation() { return dateReservation; }
    public void setDateReservation(String dateReservation) { this.dateReservation = dateReservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModalitesPaiement() { return modalitesPaiement; }
    public void setModalitesPaiement(String modalitesPaiement) { this.modalitesPaiement = modalitesPaiement; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
}