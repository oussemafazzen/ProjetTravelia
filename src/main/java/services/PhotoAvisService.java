package services;

import interfaces.IService;
import models.PhotoAvis;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoAvisService implements IService<PhotoAvis> {

    private Connection cnx;

    public PhotoAvisService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(PhotoAvis photoAvis) throws SQLException {
        String req = "INSERT INTO photo_avis (chemin_fichier, legende, id_avis) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, photoAvis.getCheminFichier());
            ps.setString(2, photoAvis.getLegende());
            ps.setInt(3, photoAvis.getIdAvis());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    photoAvis.setIdPhoto(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(PhotoAvis photoAvis) throws SQLException {
        String req = "UPDATE photo_avis SET chemin_fichier = ?, legende = ?, id_avis = ? WHERE id_photo = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, photoAvis.getCheminFichier());
            ps.setString(2, photoAvis.getLegende());
            ps.setInt(3, photoAvis.getIdAvis());
            ps.setInt(4, photoAvis.getIdPhoto());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(PhotoAvis photoAvis) throws SQLException {
        delete(photoAvis.getIdPhoto());
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM photo_avis WHERE id_photo = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<PhotoAvis> getAll() throws SQLException {
        List<PhotoAvis> photos = new ArrayList<>();
        String req = "SELECT * FROM photo_avis";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                photos.add(mapResultSetToPhoto(rs));
            }
        }
        return photos;
    }

    @Override
    public PhotoAvis getById(int id) throws SQLException {
        String req = "SELECT * FROM photo_avis WHERE id_photo = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPhoto(rs);
                }
            }
        }
        return null;
    }

    public List<PhotoAvis> getPhotosByAvis(int idAvis) throws SQLException {
        List<PhotoAvis> photos = new ArrayList<>();
        String req = "SELECT * FROM photo_avis WHERE id_avis = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idAvis);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    photos.add(mapResultSetToPhoto(rs));
                }
            }
        }
        return photos;
    }

    private PhotoAvis mapResultSetToPhoto(ResultSet rs) throws SQLException {
        PhotoAvis photo = new PhotoAvis();
        photo.setIdPhoto(rs.getInt("id_photo"));
        photo.setCheminFichier(rs.getString("chemin_fichier"));
        photo.setLegende(rs.getString("legende"));
        photo.setIdAvis(rs.getInt("id_avis"));
        return photo;
    }
}
