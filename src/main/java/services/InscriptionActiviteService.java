package services;

import interfaces.IInscriptionActiviteService;
import models.InscriptionActivite;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionActiviteService implements IInscriptionActiviteService {

    private Connection cnx;

    public InscriptionActiviteService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        if (this.cnx == null) {
            System.err.println("ERREUR: La connexion à la base de données est NULL!");
        } else {
            System.out.println("InscriptionActiviteService: Connexion établie.");
        }
    }

    @Override
    public void ajouterInscription(InscriptionActivite inscription) throws SQLException {
        String req = "INSERT INTO `inscriptionactivite`(`date_activite`, `nombre_participants`, `statut`, `id_client`, `id_activite`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setDate(1, inscription.getDateActivite());
            pstm.setInt(2, inscription.getNombreParticipants());
            pstm.setString(3, inscription.getStatut());
            pstm.setInt(4, inscription.getIdClient());
            pstm.setInt(5, inscription.getIdActivite());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<InscriptionActivite> recupToutesInscriptions() throws SQLException {
        List<InscriptionActivite> inscriptions = new ArrayList<>();
        String req = "SELECT * FROM `inscriptionactivite`";
        try {
            Statement stm = this.cnx.createStatement();

            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                InscriptionActivite i = new InscriptionActivite();
                i.setIdInscription(rs.getInt("id_inscription"));
                i.setDateActivite(rs.getDate("date_activite"));
                i.setNombreParticipants(rs.getInt("nombre_participants"));
                i.setStatut(rs.getString("statut"));
                i.setIdClient(rs.getInt("id_client"));
                i.setIdActivite(rs.getInt("id_activite"));

                inscriptions.add(i);
            }
            System.out.println("InscriptionActiviteService: " + inscriptions.size() + " inscriptions récupérées.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return inscriptions;
    }

    @Override
    public void modifierInscription(InscriptionActivite inscription) throws SQLException {
        String req = "UPDATE `inscriptionactivite` SET `date_activite`=?, `nombre_participants`=?, `statut`=?, `id_client`=?, `id_activite`=? WHERE `id_inscription`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setDate(1, inscription.getDateActivite());
            pstm.setInt(2, inscription.getNombreParticipants());
            pstm.setString(3, inscription.getStatut());
            pstm.setInt(4, inscription.getIdClient());
            pstm.setInt(5, inscription.getIdActivite());
            pstm.setInt(6, inscription.getIdInscription());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimerInscription(InscriptionActivite inscription) throws SQLException {
        if (inscription != null) {
            supprimerInscriptionParId(inscription.getIdInscription());
        }
    }

    @Override
    public void supprimerInscriptionParId(int id) throws SQLException {
        String req = "DELETE FROM `inscriptionactivite` WHERE `id_inscription`=?";
        PreparedStatement pstm = this.cnx.prepareStatement(req);
        pstm.setInt(1, id);
        pstm.executeUpdate();
    }

    @Override
    public InscriptionActivite recupParIdInscription(int id) throws SQLException {
        String req = "SELECT * FROM `inscriptionactivite` WHERE `id_inscription` = ?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                InscriptionActivite i = new InscriptionActivite();
                i.setIdInscription(rs.getInt("id_inscription"));
                i.setDateActivite(rs.getDate("date_activite"));
                i.setNombreParticipants(rs.getInt("nombre_participants"));
                i.setStatut(rs.getString("statut"));
                i.setIdClient(rs.getInt("id_client"));
                i.setIdActivite(rs.getInt("id_activite"));
                return i;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public int countAll() throws SQLException {
        String req = "SELECT COUNT(*) FROM `inscriptionactivite`";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<Object[]> getStatsByStatut() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT statut, COUNT(*) FROM `inscriptionactivite` GROUP BY statut";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }

    public List<Object[]> getInscriptionsParActivite() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT a.nom, COUNT(i.id_inscription) as total FROM `inscriptionactivite` i " +
                     "JOIN `activite` a ON i.id_activite = a.id_activite " +
                     "GROUP BY a.nom ORDER BY total DESC LIMIT 8";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }
}
