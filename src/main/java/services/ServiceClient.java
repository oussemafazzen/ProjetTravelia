package services;

import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceClient {

    public String getFullNameById(int clientId) {
        String full = queryFullName("SELECT nom, prenom FROM client WHERE id = ?", clientId);
        if (!full.equals("Client")) return full;

        return queryFullName("SELECT nom, prenom FROM client WHERE id_client = ?", clientId);
    }

    private String queryFullName(String sql, int clientId) {
        Connection cn = MyDataBase.getInstance().getCnx();
        if (cn == null) return "Client";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String out = ((nom == null ? "" : nom) + " " + (prenom == null ? "" : prenom)).trim();
                    return out.isEmpty() ? "Client" : out;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Client";
    }
}
