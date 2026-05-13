package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Avis {
    private int idAvis;
    private String commentaire;
    private int note; // 1 à 5
    private Timestamp datePublication;
    private String typeService; // 'hebergement', 'activite', 'transport'
    private int idService;
    private int idClient;

    // Advanced relational fields
    private Client client;
    private List<PhotoAvis> photos;

    public Avis() {
        this.photos = new ArrayList<>();
    }

    public Avis(String commentaire, int note, String typeService, int idService, int idClient) {
        this.commentaire = commentaire;
        this.note = note;
        this.typeService = typeService;
        this.idService = idService;
        this.idClient = idClient;
        this.datePublication = new Timestamp(System.currentTimeMillis());
        this.photos = new ArrayList<>();
    }

    public Avis(int idAvis, String commentaire, int note, Timestamp datePublication, String typeService, int idService, int idClient) {
        this.idAvis = idAvis;
        this.commentaire = commentaire;
        this.note = note;
        this.datePublication = datePublication;
        this.typeService = typeService;
        this.idService = idService;
        this.idClient = idClient;
        this.photos = new ArrayList<>();
    }

    // Getters and Setters

    public int getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(int idAvis) {
        this.idAvis = idAvis;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public Timestamp getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Timestamp datePublication) {
        this.datePublication = datePublication;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<PhotoAvis> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoAvis> photos) {
        this.photos = photos;
    }
    
    public void addPhoto(PhotoAvis photo) {
        if(this.photos == null) this.photos = new ArrayList<>();
        this.photos.add(photo);
    }

    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", note=" + note +
                ", typeService='" + typeService + '\'' +
                ", idService=" + idService +
                ", idClient=" + idClient +
                '}';
    }
}
