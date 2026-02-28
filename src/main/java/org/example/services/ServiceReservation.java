package org.example.services;

import org.example.models.Reservation;
import org.example.models.ReservationAdminRow;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation {

    private final Connection cnx;

    public ServiceReservation() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // =========================
    // CRUD (CLIENT / GENERAL)
    // =========================

    public void add(Reservation r) {
        String sql = "INSERT INTO reservation (date_reservation, statut, modalites_paiement, id_client, pays, ville) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (r.getDateReservation() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            } else {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }

            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setInt(4, r.getClientId());
            ps.setString(5, r.getPays());
            ps.setString(6, r.getVille());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur add reservation: " + e.getMessage());
        }
    }

    public void update(Reservation r) {
        String sql = "UPDATE reservation SET date_reservation=?, statut=?, modalites_paiement=?, id_client=?, pays=?, ville=? " +
                "WHERE id_reservation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (r.getDateReservation() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            } else {
                ps.setTimestamp(1, null);
            }

            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setInt(4, r.getClientId());
            ps.setString(5, r.getPays());
            ps.setString(6, r.getVille());
            ps.setInt(7, r.getIdReservation());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur update reservation: " + e.getMessage());
        }
    }

    public void delete(int idReservation) {
        try {
            // Supprimer billets d'abord (FK)
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM billet WHERE id_reservation=?")) {
                ps.setInt(1, idReservation);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM reservation WHERE id_reservation=?")) {
                ps.setInt(1, idReservation);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur delete reservation: " + e.getMessage());
        }
    }

    // ✅ UTILISÉ PAR CalendarViewController (ADMIN)
    public List<Reservation> getAll() {
        List<Reservation> out = new ArrayList<>();
        String sql = "SELECT id_reservation, date_reservation, statut, modalites_paiement, id_client, pays, ville " +
                "FROM reservation ORDER BY id_reservation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapReservation(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur getAll reservations: " + e.getMessage());
        }
        return out;
    }

    // ✅ UTILISÉ PAR CalendarViewController (CLIENT)
    public List<Reservation> getByClientId(int clientId) {
        List<Reservation> out = new ArrayList<>();
        String sql = "SELECT id_reservation, date_reservation, statut, modalites_paiement, id_client, pays, ville " +
                "FROM reservation WHERE id_client=? ORDER BY id_reservation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapReservation(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur getByClientId: " + e.getMessage());
        }
        return out;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setIdReservation(rs.getInt("id_reservation"));

        Timestamp ts = rs.getTimestamp("date_reservation");
        r.setDateReservation(ts == null ? null : ts.toLocalDateTime());

        r.setStatut(rs.getString("statut"));
        r.setModalitesPaiement(rs.getString("modalites_paiement"));
        r.setClientId(rs.getInt("id_client"));
        r.setPays(rs.getString("pays"));
        r.setVille(rs.getString("ville"));

        return r;
    }

    // =========================
    // KPI (ADMIN)
    // =========================

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM reservation";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countBilletsAll() {
        String sql = "SELECT COUNT(*) FROM billet";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double sumBilletsAll() {
        String sql = "SELECT COALESCE(SUM(prix),0) FROM billet";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // =========================
    // STATS (stats.fxml)
    // =========================

    public List<Object[]> countReservationsByStatut() {
        List<Object[]> out = new ArrayList<>();
        String sql = "SELECT statut, COUNT(*) FROM reservation GROUP BY statut ORDER BY COUNT(*) DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Object[]{rs.getString(1), rs.getInt(2)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<Object[]> topPays(int limit) {
        List<Object[]> out = new ArrayList<>();
        String sql = "SELECT pays, COUNT(*) FROM reservation " +
                "WHERE pays IS NOT NULL GROUP BY pays ORDER BY COUNT(*) DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{rs.getString(1), rs.getInt(2)});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<Object[]> reservationsParMois() {
        List<Object[]> out = new ArrayList<>();
        String sql =
                "SELECT DATE_FORMAT(date_reservation, '%Y-%m') AS mois, COUNT(*) " +
                        "FROM reservation WHERE date_reservation IS NOT NULL " +
                        "GROUP BY mois ORDER BY mois";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Object[]{rs.getString(1), rs.getInt(2)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    // =========================
    // TABLE ADMIN (MainView)
    // =========================

    public List<ReservationAdminRow> getAllAdminRows(String filter) {
        List<ReservationAdminRow> out = new ArrayList<>();

        String base =
                "SELECT r.id_reservation, r.date_reservation, r.statut, r.modalites_paiement, r.id_client, " +
                        "       COALESCE(SUM(b.prix),0) AS montant " +
                        "FROM reservation r " +
                        "LEFT JOIN billet b ON b.id_reservation = r.id_reservation ";

        String where = "";
        if (filter != null && !filter.isBlank()) {
            where = "WHERE (r.statut LIKE ? OR r.pays LIKE ? OR CAST(r.id_client AS CHAR) LIKE ?) ";
        }

        String sql = base + where +
                "GROUP BY r.id_reservation, r.date_reservation, r.statut, r.modalites_paiement, r.id_client " +
                "ORDER BY r.id_reservation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (filter != null && !filter.isBlank()) {
                String like = "%" + filter.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservationAdminRow row = new ReservationAdminRow();
                    row.setIdReservation(rs.getInt("id_reservation"));

                    Timestamp ts = rs.getTimestamp("date_reservation");
                    row.setDateReservation(ts == null ? "-" : ts.toLocalDateTime().toString());

                    row.setStatut(rs.getString("statut"));
                    row.setModalitesPaiement(rs.getString("modalites_paiement"));

                    int idClient = rs.getInt("id_client");
                    row.setClientFullName("Client #" + idClient);

                    row.setMontantTotal(rs.getDouble("montant"));

                    out.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur getAllAdminRows: " + e.getMessage());
        }

        return out;
    }
}