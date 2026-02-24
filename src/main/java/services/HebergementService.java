package services;

import interfaces.IHebergementService;
import models.Hebergement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HebergementService implements IHebergementService {

    private Connection cnx;

    public HebergementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        if (this.cnx == null) {
            System.err.println("ERREUR: La connexion à la base de données est NULL!");
        } else {
            System.out.println("HebergementService: Connexion établie.");
        }
    }

    @Override
    public void ajouterHebergement(Hebergement hebergement) throws SQLException {
        String req = "INSERT INTO `hebergement`(`nom`, `type`, `adresse`, `ville`, `pays`, `capacite`, `equipements`, `tarif_par_nuit`) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, hebergement.getNom());
            pstm.setString(2, hebergement.getType());
            pstm.setString(3, hebergement.getAdresse());
            pstm.setString(4, hebergement.getVille());
            pstm.setString(5, hebergement.getPays());
            pstm.setInt(6, hebergement.getCapacite());
            pstm.setString(7, hebergement.getEquipements());
            pstm.setDouble(8, hebergement.getTarifParNuit());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Hebergement> recupTousHebergements() throws SQLException {
        List<Hebergement> hebergements = new ArrayList<>();
        String req = "SELECT * FROM `hebergement`";
        try {
            Statement stm = this.cnx.createStatement();

            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Hebergement h = new Hebergement();
                h.setIdHebergement(rs.getInt("id_hebergement"));
                h.setNom(rs.getString("nom"));
                h.setType(rs.getString("type"));
                h.setAdresse(rs.getString("adresse"));
                h.setVille(rs.getString("ville"));
                h.setPays(rs.getString("pays"));
                h.setCapacite(rs.getInt("capacite"));
                h.setEquipements(rs.getString("equipements"));
                h.setTarifParNuit(rs.getDouble("tarif_par_nuit"));

                hebergements.add(h);
            }
            System.out.println("HebergementService: " + hebergements.size() + " hébergements récupérés.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return hebergements;
    }

    @Override
    public void modifierHebergement(Hebergement hebergement) throws SQLException {
        String req = "UPDATE `hebergement` SET `nom`=?, `type`=?, `adresse`=?, `ville`=?, `pays`=?, `capacite`=?, `equipements`=?, `tarif_par_nuit`=? WHERE `id_hebergement`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, hebergement.getNom());
            pstm.setString(2, hebergement.getType());
            pstm.setString(3, hebergement.getAdresse());
            pstm.setString(4, hebergement.getVille());
            pstm.setString(5, hebergement.getPays());
            pstm.setInt(6, hebergement.getCapacite());
            pstm.setString(7, hebergement.getEquipements());
            pstm.setDouble(8, hebergement.getTarifParNuit());
            pstm.setInt(9, hebergement.getIdHebergement());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimerHebergement(Hebergement hebergement) throws SQLException {
        if (hebergement != null) {
            supprimerHebergementParId(hebergement.getIdHebergement());
        }
    }



    @Override
    public void supprimerHebergementParId(int id) throws SQLException {
        String req = "DELETE FROM `hebergement` WHERE `id_hebergement`=?";
        PreparedStatement pstm = this.cnx.prepareStatement(req);
        pstm.setInt(1, id);
        pstm.executeUpdate();
    }

    @Override
    public Hebergement recupParIdHebergement(int id) throws SQLException {
        String req = "SELECT * FROM `hebergement` WHERE `id_hebergement` = ?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Hebergement h = new Hebergement();
                h.setIdHebergement(rs.getInt("id_hebergement"));
                h.setNom(rs.getString("nom"));
                h.setType(rs.getString("type"));
                h.setAdresse(rs.getString("adresse"));
                h.setVille(rs.getString("ville"));
                h.setPays(rs.getString("pays"));
                h.setCapacite(rs.getInt("capacite"));
                h.setEquipements(rs.getString("equipements"));
                h.setTarifParNuit(rs.getDouble("tarif_par_nuit"));
                return h;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
