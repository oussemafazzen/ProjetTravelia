package models;

import java.sql.Date;

public class InscriptionActivite {
    private int idInscription;
    private Date dateActivite;
    private int nombreParticipants;
    private String statut;
    private int idClient;
    private int idActivite;

    // Constructeur par défaut
    public InscriptionActivite() {
    }

    // Constructeur paramétré
    public InscriptionActivite(Date dateActivite, int nombreParticipants, String statut,
                               int idClient, int idActivite) {
        this.dateActivite = dateActivite;
        this.nombreParticipants = nombreParticipants;
        this.statut = statut;
        this.idClient = idClient;
        this.idActivite = idActivite;
    }

    // Getters et Setters
    public int getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(int idInscription) {
        this.idInscription = idInscription;
    }

    public Date getDateActivite() {
        return dateActivite;
    }

    public void setDateActivite(Date dateActivite) {
        this.dateActivite = dateActivite;
    }

    public int getNombreParticipants() {
        return nombreParticipants;
    }

    public void setNombreParticipants(int nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
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

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    @Override
    public String toString() {
        return "InscriptionActivite{" +
                "idInscription=" + idInscription +
                ", dateActivite=" + dateActivite +
                ", nombreParticipants=" + nombreParticipants +
                ", statut='" + statut + '\'' +
                ", idClient=" + idClient +
                ", idActivite=" + idActivite +
                '}';
    }
}
