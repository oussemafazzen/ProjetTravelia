package models;

public class PhotoAvis {
    private int idPhoto;
    private String cheminFichier;
    private String legende;
    private int idAvis;

    public PhotoAvis() {
    }

    public PhotoAvis(String cheminFichier, String legende, int idAvis) {
        this.cheminFichier = cheminFichier;
        this.legende = legende;
        this.idAvis = idAvis;
    }

    public PhotoAvis(int idPhoto, String cheminFichier, String legende, int idAvis) {
        this.idPhoto = idPhoto;
        this.cheminFichier = cheminFichier;
        this.legende = legende;
        this.idAvis = idAvis;
    }

    // Getters and Setters

    public int getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(int idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getLegende() {
        return legende;
    }

    public void setLegende(String legende) {
        this.legende = legende;
    }

    public int getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(int idAvis) {
        this.idAvis = idAvis;
    }

    @Override
    public String toString() {
        return "PhotoAvis{" +
                "idPhoto=" + idPhoto +
                ", cheminFichier='" + cheminFichier + '\'' +
                ", idAvis=" + idAvis +
                '}';
    }
}
