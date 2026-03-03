package services;

import models.Reservation;
import models.ReservationAdminRow;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation {

    private final Connection cnx;

    public ServiceReservation() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public int add(Reservation r) {
        String sql = "INSERT INTO reservation (date_reservation, statut, modalites_paiement, id_client, paysdestination) VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setInt(4, r.getClientId());
            ps.setString(5, r.getPaysdestination());
            // old_statut est NULL par défaut à l'insertion

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    r.setIdReservation(id);
                    return id;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur add Reservation: " + e.getMessage(), e);
        }

        return -1;
    }

    public void update(Reservation r) {
        String sql = "UPDATE reservation SET date_reservation=?, statut=?, modalites_paiement=?, paysdestination=? WHERE id_reservation=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setString(4, r.getPaysdestination());
            ps.setInt(5, r.getIdReservation());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Reservation: " + e.getMessage(), e);
        }
    }

    public void delete(int idReservation) {
        String sql = "DELETE FROM reservation WHERE id_reservation=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Reservation: " + e.getMessage(), e);
        }
    }

    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation ORDER BY id_reservation DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapReservation(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll Reservation: " + e.getMessage(), e);
        }

        return list;
    }

    public Reservation getById(int id) {
        String sql = "SELECT * FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getById Reservation: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Reservation> getByClientId(int clientId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE id_client = ? ORDER BY id_reservation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getByClientId: " + e.getMessage(), e);
        }

        return list;
    }

    public List<ReservationAdminRow> getAllAdminRows() {
        List<ReservationAdminRow> list = new ArrayList<>();

        String sql = """
            SELECT r.id_reservation,
                   r.date_reservation,
                   r.statut,
                   r.modalites_paiement,
                   r.id_client,
                   r.paysdestination,
                   c.nom,
                   c.prenom
            FROM reservation r
            JOIN client c ON r.id_client = c.id
            ORDER BY r.id_reservation DESC
            """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapAdminRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAllAdminRows: " + e.getMessage(), e);
        }

        return list;
    }

    public List<ReservationAdminRow> searchAdminRows(String keyword) {
        List<ReservationAdminRow> list = new ArrayList<>();

        String sql = """
            SELECT r.id_reservation,
                   r.date_reservation,
                   r.statut,
                   r.modalites_paiement,
                   r.id_client,
                   r.paysdestination,
                   c.nom,
                   c.prenom
            FROM reservation r
            JOIN client c ON r.id_client = c.id
            WHERE c.nom LIKE ?
               OR c.prenom LIKE ?
               OR r.statut LIKE ?
               OR r.modalites_paiement LIKE ?
            ORDER BY r.id_reservation DESC
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ps.setString(4, k);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAdminRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur searchAdminRows: " + e.getMessage(), e);
        }

        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM reservation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur countAll: " + e.getMessage(), e);
        }
    }

    public int countBilletsAll() {
        String sql = "SELECT COUNT(*) FROM billet";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur countBilletsAll: " + e.getMessage(), e);
        }
    }

    public double sumBilletsAll() {
        String sql = "SELECT COALESCE(SUM(prix), 0) FROM billet";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur sumBilletsAll: " + e.getMessage(), e);
        }
    }

    public boolean isConfirmed(int reservationId) {
        String sql = "SELECT COUNT(*) FROM billet WHERE id_reservation = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public String getStatutOriginal(int reservationId) {
        return isConfirmed(reservationId) ? "confirmé" : "en_attente";
    }

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
        String sql = "SELECT paysdestination, COUNT(*) FROM reservation " +
                "WHERE paysdestination IS NOT NULL GROUP BY paysdestination ORDER BY COUNT(*) DESC LIMIT ?";
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

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setIdReservation(rs.getInt("id_reservation"));
        r.setClientId(rs.getInt("id_client"));

        Timestamp ts = rs.getTimestamp("date_reservation");
        r.setDateReservation((ts != null) ? ts.toLocalDateTime() : LocalDateTime.now());

        r.setStatut(rs.getString("statut"));
        r.setModalitesPaiement(rs.getString("modalites_paiement"));
        r.setPaysdestination(rs.getString("paysdestination"));
        
        return r;
    }

    private ReservationAdminRow mapAdminRow(ResultSet rs) throws SQLException {
        ReservationAdminRow row = new ReservationAdminRow();

        row.setIdReservation(rs.getInt("id_reservation"));

        Timestamp ts = rs.getTimestamp("date_reservation");
        if (ts != null) row.setDateReservation(ts.toLocalDateTime().toLocalDate());

        row.setStatut(rs.getString("statut"));
        row.setModalitesPaiement(rs.getString("modalites_paiement"));

        row.setIdClient(rs.getInt("id_client"));
        row.setNomClient(rs.getString("nom"));
        row.setPrenomClient(rs.getString("prenom"));
        row.setPaysdestination(rs.getString("paysdestination"));

        return row;
    }
}
