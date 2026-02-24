package services;

import interfaces.IService;
import models.Client;
import models.enums.NiveauFidelite;
import models.enums.Role;
import models.enums.Statut;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDataBase;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class ClientService implements IService<Client> {

    private Connection cnx;

    public ClientService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Client client) throws SQLException {
        String req = "INSERT INTO client (nom, prenom, email, password, telephone, nationalite, date_naissance, role, statut, date_creation, derniere_connexion, points_fidelite, niveau_fidelite) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, client.getNom());
        ps.setString(2, client.getPrenom());
        ps.setString(3, client.getEmail());
        // Hash password
        String hashedPassword = BCrypt.hashpw(client.getPassword(), BCrypt.gensalt());
        ps.setString(4, hashedPassword);
        ps.setString(5, client.getTelephone());
        ps.setString(6, client.getNationalite());
        if (client.getDate_naissance() != null) {
            ps.setDate(7, new java.sql.Date(client.getDate_naissance().getTime()));
        } else {
            ps.setNull(7, Types.DATE);
        }
        ps.setString(8, client.getRole().toString());
        ps.setString(9, client.getStatut().toString());
        ps.setTimestamp(10, client.getDate_creation());
        ps.setTimestamp(11, client.getDerniere_connexion());
        ps.setInt(12, client.getPoints_fidelite());
        ps.setString(13, client.getNiveau_fidelite().toString());

        ps.executeUpdate();
        System.out.println("Client ajouté avec succès !");
    }

    @Override
    public void update(Client client) throws SQLException {
        String req = "UPDATE client SET nom=?, prenom=?, email=?, telephone=?, nationalite=?, date_naissance=?, statut=?, points_fidelite=?, niveau_fidelite=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, client.getNom());
        ps.setString(2, client.getPrenom());
        ps.setString(3, client.getEmail());
        ps.setString(4, client.getTelephone());
        ps.setString(5, client.getNationalite());
        if (client.getDate_naissance() != null) {
            ps.setDate(6, new java.sql.Date(client.getDate_naissance().getTime()));
        } else {
            ps.setNull(6, Types.DATE);
        }
        ps.setString(7, client.getStatut().toString());
        ps.setInt(8, client.getPoints_fidelite());
        ps.setString(9, client.getNiveau_fidelite().toString());
        ps.setInt(10, client.getId());

        ps.executeUpdate();
        System.out.println("Client mis à jour !");
    }

    @Override
    public void delete(Client client) throws SQLException {
        if (client != null) {
            delete(client.getId());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM client WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Client supprimé !");
    }

    @Override
    public List<Client> getAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String req = "SELECT * FROM client";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            clients.add(mapResultSetToClient(rs));
        }
        return clients;
    }

    @Override
    public Client getById(int id) throws SQLException {
        String req = "SELECT * FROM client WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSetToClient(rs);
        }
        return null;
    }

    // Advanced Business Logic

    public void addPoints(int idClient, int pointsToAdd) throws SQLException {
        Client c = getById(idClient);
        if (c != null) {
            int newPoints = c.getPoints_fidelite() + pointsToAdd;
            c.setPoints_fidelite(newPoints);
            
            // Logic for Level Upgrade
            if (newPoints >= 5000) {
                c.setNiveau_fidelite(NiveauFidelite.GOLD);
            } else if (newPoints >= 1000) {
                c.setNiveau_fidelite(NiveauFidelite.SILVER);
            } else {
                c.setNiveau_fidelite(NiveauFidelite.BRONZE);
            }

            update(c);
            System.out.println("Points ajoutés. Nouveau solde: " + newPoints + ". Niveau: " + c.getNiveau_fidelite());
        }
    }

    public void blockClient(int id) throws SQLException {
        Client c = getById(id);
        if (c != null) {
            String newStatut = c.getStatut() == Statut.BLOQUE ? "ACTIF" : "BLOQUE";
            String req = "UPDATE client SET statut=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, newStatut);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Statut du client " + id + " changé en: " + newStatut);
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String req = "SELECT * FROM client WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // --- Métiers Avancés ---

    // 1. Recherche dynamique (Nom ou Email)
    public List<Client> rechercherParNom(String nom) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String req = "SELECT * FROM client WHERE nom LIKE ? OR email LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + nom + "%");
        ps.setString(2, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            clients.add(mapResultSetToClient(rs));
        }
        return clients;
    }

    // 2. Tri par points de fidélité
    public List<Client> trierParPoints() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String req = "SELECT * FROM client ORDER BY points_fidelite DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            clients.add(mapResultSetToClient(rs));
        }
        return clients;
    }

    // 3. Statistiques par Niveau de Fidélité
    public Map<String, Integer> getStatsNiveau() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String req = "SELECT niveau_fidelite, COUNT(*) as count FROM client GROUP BY niveau_fidelite";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            stats.put(rs.getString("niveau_fidelite"), rs.getInt("count"));
        }
        return stats;
    }

    // 5. Statistiques par Nationalité
    public Map<String, Integer> getStatsNationalite() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String req = "SELECT nationalite, COUNT(*) as count FROM client GROUP BY nationalite";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            String nat = rs.getString("nationalite");
            if (nat == null || nat.isEmpty()) nat = "Inconnue";
            stats.put(nat, rs.getInt("count"));
        }
        return stats;
    }

    // 4. Export PDF
    public void exportClientPdf(Client c, String destPath) {
        try {
            PdfWriter writer = new PdfWriter(destPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Fiche Client").setFontSize(18).setBold());
            document.add(new Paragraph("Nom: " + c.getNom()));
            document.add(new Paragraph("Prénom: " + c.getPrenom()));
            document.add(new Paragraph("Email: " + c.getEmail()));
            document.add(new Paragraph("Téléphone: " + c.getTelephone()));
            document.add(new Paragraph("Points Fidelité: " + c.getPoints_fidelite()));
            document.add(new Paragraph("Niveau: " + c.getNiveau_fidelite()));
            document.add(new Paragraph("Date création: " + c.getDate_creation()));

            document.close();
            System.out.println("PDF généré avec succès : " + destPath);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // Helper method to mapping
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setEmail(rs.getString("email"));
        c.setPassword(rs.getString("password"));
        c.setTelephone(rs.getString("telephone"));
        c.setNationalite(rs.getString("nationalite"));
        c.setDate_naissance(rs.getDate("date_naissance"));
        
        String roleStr = rs.getString("role");
        try {
            c.setRole(Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            if (roleStr != null && roleStr.contains("CLIENT")) c.setRole(Role.CLIENT);
            else c.setRole(Role.USER);
        }

        c.setStatut(Statut.valueOf(rs.getString("statut")));
        c.setDate_creation(rs.getTimestamp("date_creation"));
        c.setDerniere_connexion(rs.getTimestamp("derniere_connexion"));
        c.setPoints_fidelite(rs.getInt("points_fidelite"));
        
        String niveauStr = rs.getString("niveau_fidelite");
        try {
            c.setNiveau_fidelite(NiveauFidelite.valueOf(niveauStr));
        } catch (IllegalArgumentException | NullPointerException e) {
            c.setNiveau_fidelite(NiveauFidelite.BRONZE);
        }
        
        return c;
    }
}
