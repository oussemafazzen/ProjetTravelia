package org.example.models;

import java.time.LocalDateTime;

public class Billet {

    private int idBillet;
    private String typeTransport;
    private String numeroBillet;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private double prix;
    private String statut;
    private Reservation reservation;

    public Billet() {
    }

    public Billet(int idBillet, String typeTransport, String numeroBillet,
                  LocalDateTime dateDepart, LocalDateTime dateArrivee,
                  double prix, String statut, Reservation reservation) {
        this.idBillet = idBillet;
        this.typeTransport = typeTransport;
        this.numeroBillet = numeroBillet;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.prix = prix;
        this.statut = statut;
        this.reservation = reservation;
    }

    public Billet(String typeTransport, String numeroBillet,
                  LocalDateTime dateDepart, LocalDateTime dateArrivee,
                  double prix, String statut, Reservation reservation) {
        this.typeTransport = typeTransport;
        this.numeroBillet = numeroBillet;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.prix = prix;
        this.statut = statut;
        this.reservation = reservation;
    }

    public int getIdBillet() {
        return idBillet;
    }

    public void setIdBillet(int idBillet) {
        this.idBillet = idBillet;
    }

    public String getTypeTransport() {
        return typeTransport;
    }

    public void setTypeTransport(String typeTransport) {
        this.typeTransport = typeTransport;
    }

    public String getNumeroBillet() {
        return numeroBillet;
    }

    public void setNumeroBillet(String numeroBillet) {
        this.numeroBillet = numeroBillet;
    }

    public LocalDateTime getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDateTime dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDateTime getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDateTime dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    @Override
    public String toString() {
        return "Billet{" +
                "idBillet=" + idBillet +
                ", typeTransport='" + typeTransport + '\'' +
                ", numeroBillet='" + numeroBillet + '\'' +
                ", dateDepart=" + dateDepart +
                ", dateArrivee=" + dateArrivee +
                ", prix=" + prix +
                ", statut='" + statut + '\'' +
                ", reservation=" + reservation +
                '}';
    }
}
