package models;

public class Activite {
    private int idActivite;
    private String nom;
    private String description;
    private String lieu;
    private int duree;
    private double prix;
    private int capaciteMax;
    private String categorie;

    // Constructeur par défaut
    public Activite() {
    }

    // Constructeur paramétré
    public Activite(String nom, String description, String lieu, int duree,
                    double prix, int capaciteMax, String categorie) {
        this.nom = nom;
        this.description = description;
        this.lieu = lieu;
        this.duree = duree;
        this.prix = prix;
        this.capaciteMax = capaciteMax;
        this.categorie = categorie;
    }

    // Getters et Setters
    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", lieu='" + lieu + '\'' +
                ", duree=" + duree +
                ", prix=" + prix +
                ", capaciteMax=" + capaciteMax +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}
