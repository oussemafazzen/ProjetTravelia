package org.example.services;

import org.example.interfaces.Services;
import org.example.models.Reservation;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements Services<Reservation> {

    private final Connection cnx;

    public ServiceReservation() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Reservation r) {
        String sql = "INSERT INTO reservation (date_reservation, statut, modalites_paiement, id_client) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setInt(4, r.getIdClient());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdReservation(rs.getInt("id_reservation"));

                Timestamp ts = rs.getTimestamp("date_reservation");
                if (ts != null) r.setDateReservation(ts.toLocalDateTime());

                r.setStatut(rs.getString("statut"));
                r.setModalitesPaiement(rs.getString("modalites_paiement"));
                r.setIdClient(rs.getInt("id_client"));
                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getTotalReservations() {
        String sql = "SELECT COUNT(*) FROM reservation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void update(Reservation r) {
        String sql = "UPDATE reservation SET date_reservation=?, statut=?, modalites_paiement=?, id_client=? WHERE id_reservation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(2, r.getStatut());
            ps.setString(3, r.getModalitesPaiement());
            ps.setInt(4, r.getIdClient());
            ps.setInt(5, r.getIdReservation());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Reservation r) {
        String sql = "DELETE FROM reservation WHERE id_reservation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getIdReservation());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ======================= NEW (CLIENT VIEW) ======================= */
    public List<Reservation> getByClientId(int clientId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE id_client = ? ORDER BY id_reservation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdReservation(rs.getInt("id_reservation"));

                Timestamp ts = rs.getTimestamp("date_reservation");
                if (ts != null) r.setDateReservation(ts.toLocalDateTime());

                r.setStatut(rs.getString("statut"));
                r.setModalitesPaiement(rs.getString("modalites_paiement"));
                r.setIdClient(rs.getInt("id_client"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}