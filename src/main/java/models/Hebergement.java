package models;

public class Hebergement {
    private int idHebergement;
    private String nom;
    private String type;
    private String adresse;
    private String ville;
    private String pays;
    private int capacite;
    private String equipements;
    private double tarifParNuit;

    // Constructeur par défaut
    public Hebergement() {
    }

    // Constructeur paramétré
    public Hebergement(String nom, String type, String adresse, String ville, String pays,
            int capacite, String equipements, double tarifParNuit) {
        this.nom = nom;
        this.type = type;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.capacite = capacite;
        this.equipements = equipements;
        this.tarifParNuit = tarifParNuit;
    }


    // Getters et Setters
    public int getIdHebergement() {
        return idHebergement;
    }

    public void setIdHebergement(int idHebergement) {
        this.idHebergement = idHebergement;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getEquipements() {
        return equipements;
    }

    public void setEquipements(String equipements) {
        this.equipements = equipements;
    }

    public double getTarifParNuit() {
        return tarifParNuit;
    }

    public void setTarifParNuit(double tarifParNuit) {
        this.tarifParNuit = tarifParNuit;
    }

    @Override
    public String toString() {
        return "Hebergement{" +
                "idHebergement=" + idHebergement +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", pays='" + pays + '\'' +
                ", capacite=" + capacite +
                ", equipements='" + equipements + '\'' +
                ", tarifParNuit=" + tarifParNuit +
                '}';
    }
}
