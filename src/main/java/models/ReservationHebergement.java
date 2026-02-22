package models;

import java.sql.Date;

public class ReservationHebergement {
    private int idReservationHebergement;
    private Date dateDebut, dateFin;
    private int nombrePersonnes;
    private Hebergement hebergement;
    private int idClient;
    private String statut;

    public ReservationHebergement() {
    }

    public ReservationHebergement(Date dateDebut, Date dateFin, int nombrePersonnes, String statut, int idClient,
                                  Hebergement hebergement) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombrePersonnes = nombrePersonnes;
        this.statut = statut;
        this.idClient = idClient;
        this.hebergement = hebergement;
    }

    public int getIdReservationHebergement() {
        return idReservationHebergement;
    }

    public void setIdReservationHebergement(int idReservationHebergement) {
        this.idReservationHebergement = idReservationHebergement;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public int getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(int nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public Hebergement getHebergement() {
        return hebergement;
    }

    public void setHebergement(Hebergement hebergement) {
        this.hebergement = hebergement;
    }

    @Override
    public String toString() {
        return "ReservationHebergement{" +
                "idReservationHebergement=" + idReservationHebergement +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", nombrePersonnes=" + nombrePersonnes +
                ", statut='" + statut + '\'' +
                ", idClient=" + idClient +
                ", hebergement=" + (hebergement != null ? hebergement.getNom() : "null") +
                "}\n";
    }
}
