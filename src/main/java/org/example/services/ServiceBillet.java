package org.example.services;

import org.example.models.Billet;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceBillet {

    private final Connection cnx;

    public ServiceBillet() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // =========================
    // ADD
    // =========================
    public void add(Billet b) {
        String sql = """
            INSERT INTO billet(numero_billet, type_transport, date_depart, date_arrivee, prix, statut, id_reservation)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, b.getNumeroBillet());
            ps.setString(2, b.getTypeTransport());
            ps.setTimestamp(3, Timestamp.valueOf(b.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getDateArrivee()));
            ps.setDouble(5, b.getPrix());
            ps.setString(6, b.getStatut());
            ps.setInt(7, b.getReservationId());

            ps.executeUpdate();

            // set id generated (optionnel)
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setIdBillet(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur add billet: " + e.getMessage(), e);
        }
    }

    // =========================
    // GET ALL
    // =========================
    public List<Billet> getAll() {
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet ORDER BY id_billet DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll Billet: " + e.getMessage(), e);
        }
        return list;
    }

    // =========================
    // GET BY RESERVATION
    // =========================
    public List<Billet> getByReservationId(int reservationId) {
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet WHERE id_reservation=? ORDER BY id_billet DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getByReservationId: " + e.getMessage(), e);
        }

        return list;
    }

    // =========================
    // UPDATE
    // =========================
    public void update(Billet b) {
        String sql = """
            UPDATE billet
            SET numero_billet=?, type_transport=?, date_depart=?, date_arrivee=?, prix=?, statut=?, id_reservation=?
            WHERE id_billet=?
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, b.getNumeroBillet());
            ps.setString(2, b.getTypeTransport());
            ps.setTimestamp(3, Timestamp.valueOf(b.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getDateArrivee()));
            ps.setDouble(5, b.getPrix());
            ps.setString(6, b.getStatut());
            ps.setInt(7, b.getReservationId());
            ps.setInt(8, b.getIdBillet());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Billet: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETE
    // =========================
    public void delete(int idBillet) {
        String sql = "DELETE FROM billet WHERE id_billet=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idBillet);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete billet: " + e.getMessage(), e);
        }
    }

    // =========================
    // MAPPER
    // =========================
    private Billet map(ResultSet rs) throws SQLException {
        Billet b = new Billet();
        b.setIdBillet(rs.getInt("id_billet"));
        b.setTypeTransport(rs.getString("type_transport"));
        b.setNumeroBillet(rs.getString("numero_billet"));

        Timestamp dep = rs.getTimestamp("date_depart");
        Timestamp arr = rs.getTimestamp("date_arrivee");
        b.setDateDepart(dep != null ? dep.toLocalDateTime() : LocalDateTime.now());
        b.setDateArrivee(arr != null ? arr.toLocalDateTime() : LocalDateTime.now());

        b.setPrix(rs.getDouble("prix"));
        b.setStatut(rs.getString("statut"));
        b.setReservationId(rs.getInt("id_reservation"));
        return b;
    }
}