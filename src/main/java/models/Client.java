package models;

import models.enums.NiveauFidelite;
import models.enums.Role;
import models.enums.Statut;

import java.sql.Timestamp;
import java.util.Date;

public class Client extends User {
    private String nom;
    private String prenom;
    private String telephone;
    private String nationalite;
    private Date date_naissance;

    // Advanced fields
    private Timestamp date_creation;
    private Timestamp derniere_connexion;
    private int points_fidelite;
    private NiveauFidelite niveau_fidelite;

    public Client() {
        super();
        this.role = Role.USER; // Default role
        this.statut = Statut.ACTIF; // Default status
        this.date_creation = new Timestamp(System.currentTimeMillis());
        this.points_fidelite = 0;
        this.niveau_fidelite = NiveauFidelite.BRONZE;
    }

    public Client(String email, String password, Role role, Statut statut, String nom, String prenom, String telephone, String nationalite, Date date_naissance) {
        super(email, password, role, statut);
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.nationalite = nationalite;
        this.date_naissance = date_naissance;
        this.date_creation = new Timestamp(System.currentTimeMillis());
        this.points_fidelite = 0;
        this.niveau_fidelite = NiveauFidelite.BRONZE;
    }

    public Client(int id, String email, String password, Role role, Statut statut, String nom, String prenom, String telephone, String nationalite, Date date_naissance, Timestamp date_creation, Timestamp derniere_connexion, int points_fidelite, NiveauFidelite niveau_fidelite) {
        super(id, email, password, role, statut);
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.nationalite = nationalite;
        this.date_naissance = date_naissance;
        this.date_creation = date_creation;
        this.derniere_connexion = derniere_connexion;
        this.points_fidelite = points_fidelite;
        this.niveau_fidelite = niveau_fidelite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public Date getDate_naissance() {
        return date_naissance;
    }

    public void setDate_naissance(Date date_naissance) {
        this.date_naissance = date_naissance;
    }

    public Timestamp getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Timestamp date_creation) {
        this.date_creation = date_creation;
    }

    public Timestamp getDerniere_connexion() {
        return derniere_connexion;
    }

    public void setDerniere_connexion(Timestamp derniere_connexion) {
        this.derniere_connexion = derniere_connexion;
    }

    public int getPoints_fidelite() {
        return points_fidelite;
    }

    public void setPoints_fidelite(int points_fidelite) {
        this.points_fidelite = points_fidelite;
    }

    public NiveauFidelite getNiveau_fidelite() {
        return niveau_fidelite;
    }

    public void setNiveau_fidelite(NiveauFidelite niveau_fidelite) {
        this.niveau_fidelite = niveau_fidelite;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                ", points=" + points_fidelite +
                ", niveau=" + niveau_fidelite +
                '}';
    }
}
