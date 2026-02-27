package org.example.models;

import java.time.LocalDateTime;

public class Billet {
    private int idBillet;
    private int reservationId;
    private String typeTransport;
    private String numeroBillet;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private double prix;
    private String statut;

    public int getIdBillet() { return idBillet; }
    public void setIdBillet(int idBillet) { this.idBillet = idBillet; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public String getTypeTransport() { return typeTransport; }
    public void setTypeTransport(String typeTransport) { this.typeTransport = typeTransport; }

    public String getNumeroBillet() { return numeroBillet; }
    public void setNumeroBillet(String numeroBillet) { this.numeroBillet = numeroBillet; }

    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime dateDepart) { this.dateDepart = dateDepart; }

    public LocalDateTime getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDateTime dateArrivee) { this.dateArrivee = dateArrivee; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}