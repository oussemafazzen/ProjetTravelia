package services;

import interfaces.IService;
import models.ReservationHebergement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationHebergementService implements IService<ReservationHebergement> {

    private Connection cnx;

    public ReservationHebergementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(ReservationHebergement reservation) {
        String req = "INSERT INTO `reservationhebergement`(`date_debut`, `date_fin`, `nombre_personnes`, `statut`, `id_client`, `id_hebergement`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setDate(1, reservation.getDateDebut());
            pstm.setDate(2, reservation.getDateFin());
            pstm.setInt(3, reservation.getNombrePersonnes());
            pstm.setString(4, reservation.getStatut());
            pstm.setInt(5, reservation.getIdClient());
            pstm.setInt(6, reservation.getHebergement().getIdHebergement());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<ReservationHebergement> getAll() {
        List<ReservationHebergement> reservations = new ArrayList<>();
        HebergementService hs = new HebergementService();
        String req = "SELECT * FROM `reservationhebergement`";
        try {
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
                r.setHebergement(hs.getById(idH));

                reservations.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return reservations;
    }

    @Override
    public void update(ReservationHebergement reservation) {
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
    public void delete(ReservationHebergement reservation) {
        String req = "DELETE FROM `reservationhebergement` WHERE `id_reservation_hebergement`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, reservation.getIdReservationHebergement());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
