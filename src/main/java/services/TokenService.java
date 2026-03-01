package services;

import models.PasswordResetToken;
import utils.MyDataBase;

import java.security.SecureRandom;
import java.sql.*;

public class TokenService {

    private Connection cnx;

    public TokenService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    /**
     * Génère un token UUID sécurisé, le stocke en BD avec une expiration d'1 heure.
     */
    public String generateToken(int userId) throws SQLException {
        try {
            String deleteReq = "UPDATE password_reset_token SET used = TRUE WHERE user_id = ? AND used = FALSE";
            PreparedStatement psDelete = cnx.prepareStatement(deleteReq);
            psDelete.setInt(1, userId);
            psDelete.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ERREUR validation table password_reset_token: " + e.getMessage());
            throw e; // Rethrow to show in UI
        }

        // Generate 6-digit random number
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String token = String.valueOf(code);
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 3600000); // 1 hour

        String req = "INSERT INTO password_reset_token (user_id, token, expiry_date, used) VALUES (?, ?, ?, FALSE)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, userId);
        ps.setString(2, token);
        ps.setTimestamp(3, expiry);
        ps.executeUpdate();

        System.out.println("Token généré pour userId=" + userId + " : " + token);
        return token;
    }

    /**
     * Valide un token : vérifie qu'il existe, n'est pas utilisé et non expiré.
     */
    public PasswordResetToken validateToken(String token) throws SQLException {
        String req = "SELECT * FROM password_reset_token WHERE token = ? AND used = FALSE AND expiry_date > NOW()";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, token);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            PasswordResetToken prt = new PasswordResetToken();
            prt.setId(rs.getInt("id"));
            prt.setUserId(rs.getInt("user_id"));
            prt.setToken(rs.getString("token"));
            prt.setExpiryDate(rs.getTimestamp("expiry_date"));
            prt.setUsed(rs.getBoolean("used"));
            prt.setCreatedAt(rs.getTimestamp("created_at"));
            return prt;
        }
        return null;
    }

    /**
     * Marque un token comme utilisé après réinitialisation du mot de passe.
     */
    public void invalidateToken(String token) throws SQLException {
        String req = "UPDATE password_reset_token SET used = TRUE WHERE token = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, token);
        ps.executeUpdate();
        System.out.println("Token invalidé : " + token);
    }

    /**
     * Supprime tous les tokens expirés pour nettoyage.
     */
    public void deleteExpiredTokens() throws SQLException {
        String req = "DELETE FROM password_reset_token WHERE expiry_date < NOW() OR used = TRUE";
        Statement st = cnx.createStatement();
        int deleted = st.executeUpdate(req);
        System.out.println("Tokens expirés supprimés : " + deleted);
    }

    /**
     * Récupère le userId associé à un token valide.
     */
    public int getUserIdByToken(String token) throws SQLException {
        PasswordResetToken prt = validateToken(token);
        return prt != null ? prt.getUserId() : -1;
    }
}
