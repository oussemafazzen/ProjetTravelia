package models;

import java.sql.Timestamp;

public class Paiement {
    private int idPaiement;
    private Timestamp datePaiement;
    private double montant;
    private String methodePaiement;
    private int idReservation;

    public Paiement() {}

    public Paiement(double montant, String methodePaiement, int idReservation) {
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.idReservation = idReservation;
    }

    public int getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
    }

    public Timestamp getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Timestamp datePaiement) {
        this.datePaiement = datePaiement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", datePaiement=" + datePaiement +
                ", montant=" + montant +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", idReservation=" + idReservation +
                '}';
    }
}
