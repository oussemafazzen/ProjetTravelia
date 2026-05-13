package services;

import interfaces.IActiviteService;
import models.Activite;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService implements IActiviteService {

    private Connection cnx;

    public ActiviteService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        if (this.cnx == null) {
            System.err.println("ERREUR: La connexion à la base de données est NULL!");
        } else {
            System.out.println("ActiviteService: Connexion établie.");
        }
    }

    @Override
    public void ajouterActivite(Activite activite) throws SQLException {
        String req = "INSERT INTO `activite`(`nom`, `description`, `lieu`, `duree`, `prix`, `capacite_max`, `categorie`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, activite.getNom());
            pstm.setString(2, activite.getDescription());
            pstm.setString(3, activite.getLieu());
            pstm.setInt(4, activite.getDuree());
            pstm.setDouble(5, activite.getPrix());
            pstm.setInt(6, activite.getCapaciteMax());
            pstm.setString(7, activite.getCategorie());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Activite> recupToutesActivites() throws SQLException {
        List<Activite> activites = new ArrayList<>();
        String req = "SELECT * FROM `activite`";
        try {
            Statement stm = this.cnx.createStatement();

            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Activite a = new Activite();
                a.setIdActivite(rs.getInt("id_activite"));
                a.setNom(rs.getString("nom"));
                a.setDescription(rs.getString("description"));
                a.setLieu(rs.getString("lieu"));
                a.setDuree(rs.getInt("duree"));
                a.setPrix(rs.getDouble("prix"));
                a.setCapaciteMax(rs.getInt("capacite_max"));
                a.setCategorie(rs.getString("categorie"));

                activites.add(a);
            }
            System.out.println("ActiviteService: " + activites.size() + " activités récupérées.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return activites;
    }

    @Override
    public void modifierActivite(Activite activite) throws SQLException {
        String req = "UPDATE `activite` SET `nom`=?, `description`=?, `lieu`=?, `duree`=?, `prix`=?, `capacite_max`=?, `categorie`=? WHERE `id_activite`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, activite.getNom());
            pstm.setString(2, activite.getDescription());
            pstm.setString(3, activite.getLieu());
            pstm.setInt(4, activite.getDuree());
            pstm.setDouble(5, activite.getPrix());
            pstm.setInt(6, activite.getCapaciteMax());
            pstm.setString(7, activite.getCategorie());
            pstm.setInt(8, activite.getIdActivite());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimerActivite(Activite activite) throws SQLException {
        if (activite != null) {
            supprimerActiviteParId(activite.getIdActivite());
        }
    }

    @Override
    public void supprimerActiviteParId(int id) throws SQLException {
        String req = "DELETE FROM `activite` WHERE `id_activite`=?";
        PreparedStatement pstm = this.cnx.prepareStatement(req);
        pstm.setInt(1, id);
        pstm.executeUpdate();
    }

    @Override
    public Activite recupParIdActivite(int id) throws SQLException {
        String req = "SELECT * FROM `activite` WHERE `id_activite` = ?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Activite a = new Activite();
                a.setIdActivite(rs.getInt("id_activite"));
                a.setNom(rs.getString("nom"));
                a.setDescription(rs.getString("description"));
                a.setLieu(rs.getString("lieu"));
                a.setDuree(rs.getInt("duree"));
                a.setPrix(rs.getDouble("prix"));
                a.setCapaciteMax(rs.getInt("capacite_max"));
                a.setCategorie(rs.getString("categorie"));
                return a;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public int countAll() throws SQLException {
        String req = "SELECT COUNT(*) FROM `activite`";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<Object[]> getStatsByCategorie() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT categorie, COUNT(*) FROM `activite` GROUP BY categorie";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }

    public List<Object[]> getAvgPrixByCategorie() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT categorie, AVG(prix) FROM `activite` GROUP BY categorie";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getDouble(2)});
        }
        return out;
    }
}
