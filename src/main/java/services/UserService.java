package services;

import models.User;
import models.Client;
import models.Administrateur;
import models.enums.Role;
import models.enums.Statut;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDataBase;

import java.sql.*;

public class UserService {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public User login(String email, String password) throws SQLException {
        // All users (Admins and Clients) are stored in the 'client' table
        return checkLoginInTable("client", email, password);
    }

    private User checkLoginInTable(String tableName, String email, String password) throws SQLException {
         String req = "SELECT * FROM " + tableName + " WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            if (rs.getString("statut").equals("BLOQUE")) {
                System.out.println("Compte bloqué !");
                return null;
            }

            if (BCrypt.checkpw(password, rs.getString("password"))) {
                // Reset failed attempts
                String resetReq = "UPDATE " + tableName + " SET failed_attempts = 0 WHERE email = ?";
                PreparedStatement psReset = cnx.prepareStatement(resetReq);
                psReset.setString(1, email);
                psReset.executeUpdate();
                
                System.out.println("Login success from " + tableName + "!");
                
                String roleStr = rs.getString("role");
                Role finalRole = Role.USER;
                try {
                    if (roleStr != null) finalRole = Role.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("Rôle inconnu dans la DB: " + roleStr + ", utilisation de USER par défaut.");
                }

                Statut finalStatut = Statut.ACTIF;
                String statutStr = rs.getString("statut");
                try {
                    if (statutStr != null) finalStatut = Statut.valueOf(statutStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("Statut inconnu dans la DB: " + statutStr + ", utilisation de ACTIF par défaut.");
                }

                if (finalRole == Role.CLIENT || finalRole == Role.USER || tableName.equalsIgnoreCase("client")) {
                     Client client = new Client();
                     client.setId(rs.getInt("id"));
                     client.setEmail(rs.getString("email"));
                     client.setRole(finalRole);
                     client.setStatut(finalStatut);
                     client.setNom(rs.getString("nom"));
                     client.setPrenom(rs.getString("prenom"));
                     client.setTelephone(rs.getString("telephone"));
                     client.setNationalite(rs.getString("nationalite"));
                     client.setDate_naissance(rs.getDate("date_naissance"));
                     client.setPoints_fidelite(rs.getInt("points_fidelite"));
                     
                     String niveauStr = rs.getString("niveau_fidelite");
                     models.enums.NiveauFidelite niveau = models.enums.NiveauFidelite.BRONZE;
                     try {
                         if (niveauStr != null) niveau = models.enums.NiveauFidelite.valueOf(niveauStr);
                     } catch (IllegalArgumentException e) {
                         System.err.println("NiveauFidelite inconnu: " + niveauStr + ", utilisation de BRONZE.");
                     }
                     client.setNiveau_fidelite(niveau);
                     return client; 
                } else {
                     return new models.Administrateur(rs.getInt("id"), rs.getString("email"), null, finalRole, finalStatut);
                }

            } else {
                // Increment failed attempts
                int newAttempts = rs.getInt("failed_attempts") + 1;
                String updateReq = "UPDATE " + tableName + " SET failed_attempts = ? WHERE email = ?";
                PreparedStatement psUpdate = cnx.prepareStatement(updateReq);
                psUpdate.setInt(1, newAttempts);
                psUpdate.setString(2, email);
                psUpdate.executeUpdate();
                
                System.out.println("Mot de passe incorrect. Tentative: " + newAttempts);
                return null;
            }
        }
        return null;
    }
    
    public boolean emailExists(String email) throws SQLException {
        String req = "SELECT * FROM client WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM client WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            if (rs.getString("statut").equals("BLOQUE")) {
                return null;
            }

            String roleStr = rs.getString("role");
            models.enums.Role finalRole = models.enums.Role.USER;
            try {
                if (roleStr != null) finalRole = models.enums.Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Rôle inconnu dans la DB: " + roleStr);
            }

            Statut finalStatut = Statut.ACTIF;
            String statutStr = rs.getString("statut");
            try {
                if (statutStr != null) finalStatut = Statut.valueOf(statutStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Statut inconnu: " + statutStr);
            }

            models.Client client = new models.Client();
            client.setId(rs.getInt("id"));
            client.setEmail(rs.getString("email"));
            client.setRole(finalRole);
            client.setStatut(finalStatut);
            client.setNom(rs.getString("nom"));
            client.setPrenom(rs.getString("prenom"));
            client.setTelephone(rs.getString("telephone"));
            client.setNationalite(rs.getString("nationalite"));
            client.setDate_naissance(rs.getDate("date_naissance"));
            client.setPoints_fidelite(rs.getInt("points_fidelite"));
            
            String niveauStr = rs.getString("niveau_fidelite");
            models.enums.NiveauFidelite niveau = models.enums.NiveauFidelite.BRONZE;
            try {
                if (niveauStr != null) niveau = models.enums.NiveauFidelite.valueOf(niveauStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Niveau inconnu: " + niveauStr);
            }
            client.setNiveau_fidelite(niveau);
            return client;
        }
        return null;
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        String req = "UPDATE client SET password = ? WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, hashedPassword);
        ps.setString(2, email);
        ps.executeUpdate();
    }
}
