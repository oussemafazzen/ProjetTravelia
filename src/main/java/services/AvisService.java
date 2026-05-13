package services;

import interfaces.IService;
import models.Avis;
import models.Client;
import models.PhotoAvis;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisService implements IService<Avis> {

    private Connection cnx;
    private PhotoAvisService photoService;
    private UserService userService;

    public AvisService() {
        cnx = MyDataBase.getInstance().getConnection();
        photoService = new PhotoAvisService();
        userService = new UserService();
    }

    @Override
    public void add(Avis avis) throws SQLException {
        String req = "INSERT INTO avis (commentaire, note, type_service, id_service, id_client) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, avis.getCommentaire());
            ps.setInt(2, avis.getNote());
            ps.setString(3, avis.getTypeService());
            ps.setInt(4, avis.getIdService());
            ps.setInt(5, avis.getIdClient());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    avis.setIdAvis(rs.getInt(1));
                    
                    // Also save attached photos if any
                    if (avis.getPhotos() != null && !avis.getPhotos().isEmpty()) {
                        for (PhotoAvis photo : avis.getPhotos()) {
                            photo.setIdAvis(avis.getIdAvis());
                            photoService.add(photo);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update(Avis avis) throws SQLException {
        String req = "UPDATE avis SET commentaire = ?, note = ?, type_service = ?, id_service = ? WHERE id_avis = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, avis.getCommentaire());
            ps.setInt(2, avis.getNote());
            ps.setString(3, avis.getTypeService());
            ps.setInt(4, avis.getIdService());
            ps.setInt(5, avis.getIdAvis());
            ps.executeUpdate();
            
            // Note: In a complete implementation, we'd also sync the photos list here
            // (delete removed photos, add new ones).
        }
    }

    @Override
    public void delete(Avis avis) throws SQLException {
        delete(avis.getIdAvis());
    }

    @Override
    public void delete(int id) throws SQLException {
        // Since ON DELETE CASCADE is set in the DB, deleting the Avis will automatically delete the PhotoAvis records
        String req = "DELETE FROM avis WHERE id_avis = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Avis> getAll() throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String req = "SELECT * FROM avis ORDER BY date_publication DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(mapResultSetToAvis(rs));
            }
        }
        return liste;
    }

    @Override
    public Avis getById(int id) throws SQLException {
        String req = "SELECT * FROM avis WHERE id_avis = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAvis(rs);
                }
            }
        }
        return null;
    }

    // Helper mapping function to convert ResultSet to Avis object and load its relations
    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        Avis avis = new Avis();
        avis.setIdAvis(rs.getInt("id_avis"));
        avis.setCommentaire(rs.getString("commentaire"));
        avis.setNote(rs.getInt("note"));
        avis.setDatePublication(rs.getTimestamp("date_publication"));
        avis.setTypeService(rs.getString("type_service"));
        avis.setIdService(rs.getInt("id_service"));
        avis.setIdClient(rs.getInt("id_client"));
        
        // Load related client
        User u = userService.getUserById(avis.getIdClient());
        if (u instanceof Client) {
            avis.setClient((Client) u);
        }

        // Load related photos
        List<PhotoAvis> photos = photoService.getPhotosByAvis(avis.getIdAvis());
        avis.setPhotos(photos);

        return avis;
    }
}
