package services;

import models.SecurityLog;
import utils.MyDataBase;

import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SecurityLogService {

    private Connection cnx;

    public SecurityLogService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    /**
     * Enregistre un événement de sécurité dans le journal.
     */
    public void logEvent(int userId, String eventType, String details) {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String req = "INSERT INTO security_log (user_id, event_type, ip_address, details) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(req);
            if (userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, eventType);
            ps.setString(3, ipAddress);
            ps.setString(4, details);
            ps.executeUpdate();
            System.out.println("[SECURITY LOG] " + eventType + " - " + details);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement du log de sécurité: " + e.getMessage());
        }
    }

    /**
     * Récupère les derniers logs de sécurité.
     */
    public List<SecurityLog> getRecentLogs(int limit) throws SQLException {
        List<SecurityLog> logs = new ArrayList<>();
        String req = "SELECT * FROM security_log ORDER BY created_at DESC LIMIT ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            SecurityLog log = new SecurityLog();
            log.setId(rs.getInt("id"));
            log.setUserId(rs.getInt("user_id"));
            log.setEventType(rs.getString("event_type"));
            log.setIpAddress(rs.getString("ip_address"));
            log.setDetails(rs.getString("details"));
            log.setCreatedAt(rs.getTimestamp("created_at"));
            logs.add(log);
        }
        return logs;
    }

    /**
     * Récupère les logs d'un utilisateur spécifique.
     */
    public List<SecurityLog> getLogsByUserId(int userId) throws SQLException {
        List<SecurityLog> logs = new ArrayList<>();
        String req = "SELECT * FROM security_log WHERE user_id = ? ORDER BY created_at DESC";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            SecurityLog log = new SecurityLog();
            log.setId(rs.getInt("id"));
            log.setUserId(rs.getInt("user_id"));
            log.setEventType(rs.getString("event_type"));
            log.setIpAddress(rs.getString("ip_address"));
            log.setDetails(rs.getString("details"));
            log.setCreatedAt(rs.getTimestamp("created_at"));
            logs.add(log);
        }
        return logs;
    }
}
