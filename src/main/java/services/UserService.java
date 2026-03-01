package services;

import models.User;
import models.Client;
import models.Administrateur;
import models.enums.Role;
import models.enums.Statut;
import models.enums.NiveauFidelite;
import at.favre.lib.crypto.bcrypt.BCrypt;
import utils.MyDataBase;

import java.sql.*;

public class UserService {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        Role role = Role.USER;
        try {
            if (roleStr != null) {
                role = Role.valueOf(roleStr);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Rôle inconnu: " + roleStr);
        }

        Statut statut = Statut.ACTIF;
        try {
            String statStr = rs.getString("statut");
            if (statStr != null) {
                statut = Statut.valueOf(statStr);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Statut inconnu: " + rs.getString("statut"));
        }

        User user;
        if (role == Role.ADMINISTRATEUR) {
            user = new Administrateur();
        } else {
            Client client = new Client();
            client.setNom(rs.getString("nom"));
            client.setPrenom(rs.getString("prenom"));
            client.setTelephone(rs.getString("telephone"));
            client.setNationalite(rs.getString("nationalite"));
            client.setDate_naissance(rs.getDate("date_naissance"));
            client.setPoints_fidelite(rs.getInt("points_fidelite"));
            
            String niveauStr = rs.getString("niveau_fidelite");
            try {
                if (niveauStr != null) {
                    client.setNiveau_fidelite(NiveauFidelite.valueOf(niveauStr));
                }
            } catch (IllegalArgumentException e) {
                client.setNiveau_fidelite(NiveauFidelite.BRONZE);
            }
            user = client;
        }

        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(role);
        user.setStatut(statut);
        user.setFailed_attempts(rs.getInt("failed_attempts"));
        user.setGoogleId(rs.getString("google_id"));
        user.setEmailConfirmed(rs.getBoolean("email_confirmed"));

        return user;
    }

    public User login(String email, String password) throws SQLException {
        return checkLoginInTable("client", email, password);
    }

    private User checkLoginInTable(String tableName, String email, String password) throws SQLException {
        String req = "SELECT * FROM " + tableName + " WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (Statut.BLOQUE.toString().equals(rs.getString("statut"))) {
                        System.out.println("Compte bloqué !");
                        return null;
                    }

                    String storedHash = rs.getString("password");
                    if (storedHash != null && BCrypt.verifyer().verify(password.toCharArray(), storedHash).verified) {
                        // Reset failed attempts
                        String resetReq = "UPDATE " + tableName + " SET failed_attempts = 0 WHERE email = ?";
                        try (PreparedStatement psReset = cnx.prepareStatement(resetReq)) {
                            psReset.setString(1, email);
                            psReset.executeUpdate();
                        }
                        return mapResultSetToUser(rs);
                    } else {
                        // Increment failed attempts
                        int newAttempts = rs.getInt("failed_attempts") + 1;
                        String updateReq = "UPDATE " + tableName + " SET failed_attempts = ? WHERE email = ?";
                        try (PreparedStatement psUpdate = cnx.prepareStatement(updateReq)) {
                            psUpdate.setInt(1, newAttempts);
                            psUpdate.setString(2, email);
                            psUpdate.executeUpdate();
                        }
                        System.out.println("Mot de passe incorrect.");
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String req = "SELECT 1 FROM client WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM client WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        String req = "UPDATE client SET password = ? WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public void confirmEmail(int userId) throws SQLException {
        String req = "UPDATE client SET email_confirmed = TRUE WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public User loginByGoogleId(String googleId) throws SQLException {
        String req = "SELECT * FROM client WHERE google_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public User getUserById(int id) throws SQLException {
        String req = "SELECT * FROM client WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
}
