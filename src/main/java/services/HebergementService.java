package services;

import interfaces.IHebergementService;
import models.Hebergement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public int countAll() throws SQLException {
        String req = "SELECT COUNT(*) FROM `hebergement`";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<Object[]> getStatsType() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT type, COUNT(*) FROM `hebergement` GROUP BY type";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }

    public List<Object[]> getStatsPays() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT pays, COUNT(*) FROM `hebergement` GROUP BY pays";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }

    public Map<String, Map<String, Integer>> getStatsGroupedByPays() throws SQLException {
        Map<String, Map<String, Integer>> out = new HashMap<>();
        String req = "SELECT pays, type, COUNT(*) FROM `hebergement` GROUP BY pays, type";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            String pays = rs.getString(1);
            if (pays == null) pays = "Inconnu";
            String rawType = rs.getString(2).toLowerCase();
            String type = (rawType.contains("hotel") || rawType.contains("hôtel")) ? "Hôtels" : "Auberges";
            
            out.putIfAbsent(pays, new HashMap<>());
            out.get(pays).put(type, out.get(pays).getOrDefault(type, 0) + rs.getInt(3));
        }
        return out;
    }
}
