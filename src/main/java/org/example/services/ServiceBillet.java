package org.example.services;

import org.example.interfaces.Services;
import org.example.models.Billet;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceBillet implements Services<Billet> {

    private final Connection cnx;

    public ServiceBillet() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Billet b) {
        String sql = "INSERT INTO billet(type_transport, numero_billet, date_depart, date_arrivee, prix, statut, id_reservation) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, b.getTypeTransport());
            ps.setString(2, b.getNumeroBillet());
            ps.setTimestamp(3, b.getDateDepart() == null ? null : Timestamp.valueOf(b.getDateDepart()));
            ps.setTimestamp(4, b.getDateArrivee() == null ? null : Timestamp.valueOf(b.getDateArrivee()));
            ps.setDouble(5, b.getPrix());
            ps.setString(6, b.getStatut());

            // ✅ id_reservation safe
            int idRes = 0;

            // si ton modèle a un champ reservation (objet)
            if (b.getReservation() != null) {
                idRes = b.getReservation().getIdReservation();
            }

            // si jamais idRes est toujours 0 -> erreur claire
            if (idRes <= 0) {
                throw new SQLException("id_reservation manquant (Billet.reservation est null ou id invalide)");
            }

            ps.setInt(7, idRes);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Billet> getAll() {
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Billet b = new Billet();
                b.setIdBillet(rs.getInt("id_billet"));
                b.setTypeTransport(rs.getString("type_transport"));
                b.setNumeroBillet(rs.getString("numero_billet"));

                Timestamp dep = rs.getTimestamp("date_depart");
                if (dep != null) b.setDateDepart(dep.toLocalDateTime());

                Timestamp arr = rs.getTimestamp("date_arrivee");
                if (arr != null) b.setDateArrivee(arr.toLocalDateTime());

                b.setPrix(rs.getDouble("prix"));
                b.setStatut(rs.getString("statut"));
                list.add(b);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Billet> getByReservationId(int reservationId) {
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet WHERE id_reservation=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Billet b = new Billet();
                b.setIdBillet(rs.getInt("id_billet"));
                b.setTypeTransport(rs.getString("type_transport"));
                b.setNumeroBillet(rs.getString("numero_billet"));

                Timestamp dep = rs.getTimestamp("date_depart");
                if (dep != null) b.setDateDepart(dep.toLocalDateTime());

                Timestamp arr = rs.getTimestamp("date_arrivee");
                if (arr != null) b.setDateArrivee(arr.toLocalDateTime());

                b.setPrix(rs.getDouble("prix"));
                b.setStatut(rs.getString("statut"));
                list.add(b);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getTotalBillets() {
        String sql = "SELECT COUNT(*) FROM billet";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getMontantTotal() {
        String sql = "SELECT COALESCE(SUM(prix),0) FROM billet";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void update(Billet b) {}

    @Override
    public void delete(Billet b) {}
}