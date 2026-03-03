package services;

import interfaces.IReservationService;
import models.ReservationHebergement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationHebergementService implements IReservationService {

    private Connection cnx;

    public ReservationHebergementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public int ajouterReservation(ReservationHebergement reservation) throws SQLException {
        String req = "INSERT INTO `reservationhebergement`(`date_debut`, `date_fin`, `nombre_personnes`, `statut`, `id_client`, `id_hebergement`) VALUES (?,?,?,?,?,?)";
        PreparedStatement pstm = this.cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pstm.setDate(1, reservation.getDateDebut());
        pstm.setDate(2, reservation.getDateFin());
        pstm.setInt(3, reservation.getNombrePersonnes());
        pstm.setString(4, reservation.getStatut());
        pstm.setInt(5, reservation.getIdClient());
        pstm.setInt(6, reservation.getHebergement().getIdHebergement());

        pstm.executeUpdate();
        
        ResultSet rs = pstm.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    @Override
    public List<ReservationHebergement> recupToutesReservations() throws SQLException {
        List<ReservationHebergement> reservations = new ArrayList<>();
        HebergementService hs = new HebergementService();
        String req = "SELECT * FROM `reservationhebergement`";
        Statement stm = this.cnx.createStatement();

        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            ReservationHebergement r = new ReservationHebergement();
            r.setIdReservationHebergement(rs.getInt("id_reservation_hebergement"));
            r.setDateDebut(rs.getDate("date_debut"));
            r.setDateFin(rs.getDate("date_fin"));
            r.setNombrePersonnes(rs.getInt("nombre_personnes"));
            r.setStatut(rs.getString("statut"));
            r.setIdClient(rs.getInt("id_client"));
            
            int idH = rs.getInt("id_hebergement");
            r.setHebergement(hs.recupParIdHebergement(idH));

            reservations.add(r);
        }

        return reservations;
    }

    @Override
    public void modifierReservation(ReservationHebergement reservation) throws SQLException {
        String req = "UPDATE `reservationhebergement` SET `date_debut`=?, `date_fin`=?, `nombre_personnes`=?, `statut`=?, `id_client`=?, `id_hebergement`=? WHERE `id_reservation_hebergement`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setDate(1, reservation.getDateDebut());
            pstm.setDate(2, reservation.getDateFin());
            pstm.setInt(3, reservation.getNombrePersonnes());
            pstm.setString(4, reservation.getStatut());
            pstm.setInt(5, reservation.getIdClient());
            pstm.setInt(6, reservation.getHebergement().getIdHebergement());
            pstm.setInt(7, reservation.getIdReservationHebergement());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimerReservation(ReservationHebergement reservation) throws SQLException {
        if (reservation != null) {
            supprimerReservationParId(reservation.getIdReservationHebergement());
        }
    }
    @Override
    public void supprimerReservationParId(int id) throws SQLException {
        String req = "DELETE FROM `reservationhebergement` WHERE `id_reservation_hebergement`=?";
        PreparedStatement pstm = this.cnx.prepareStatement(req);
        pstm.setInt(1, id);
        pstm.executeUpdate();
    }

    @Override
    public ReservationHebergement recupParIdReservation(int id) throws SQLException {
        String req = "SELECT * FROM `reservationhebergement` WHERE `id_reservation_hebergement` = ?";
        PreparedStatement pstm = this.cnx.prepareStatement(req);
        pstm.setInt(1, id);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            ReservationHebergement r = new ReservationHebergement();
            r.setIdReservationHebergement(rs.getInt("id_reservation_hebergement"));
            r.setDateDebut(rs.getDate("date_debut"));
            r.setDateFin(rs.getDate("date_fin"));
            r.setNombrePersonnes(rs.getInt("nombre_personnes"));
            r.setStatut(rs.getString("statut"));
            r.setIdClient(rs.getInt("id_client"));
            
            HebergementService hs = new HebergementService();
            r.setHebergement(hs.recupParIdHebergement(rs.getInt("id_hebergement")));
            return r;
        }
        return null;
    }

    public int countAll() throws SQLException {
        String req = "SELECT COUNT(*) FROM `reservationhebergement`";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<Object[]> getStatsStatut() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        String req = "SELECT statut, COUNT(*) FROM `reservationhebergement` GROUP BY statut";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            out.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return out;
    }

    public Map<String, Integer> getStatsSaisonnieres() throws SQLException {
        Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        stats.put("Printemps 🌸", 0);
        stats.put("Été ☀️", 0);
        stats.put("Automne 🍂", 0);
        stats.put("Hiver ❄️", 0);

        String req = "SELECT date_debut FROM `reservationhebergement` WHERE statut LIKE '%confirm%'";
        Statement stm = this.cnx.createStatement();
        ResultSet rs = stm.executeQuery(req);
        while (rs.next()) {
            java.sql.Date d = rs.getDate(1);
            if (d == null) continue;
            int month = d.toLocalDate().getMonthValue();
            String season;
            if (month >= 3 && month <= 5) season = "Printemps 🌸";
            else if (month >= 6 && month <= 8) season = "Été ☀️";
            else if (month >= 9 && month <= 11) season = "Automne 🍂";
            else season = "Hiver ❄️";
            stats.put(season, stats.get(season) + 1);
        }
        return stats;
    }
}
