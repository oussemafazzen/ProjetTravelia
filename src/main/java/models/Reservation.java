package models;

import java.time.LocalDateTime;

public class Reservation {

    private int idReservation;
    private int clientId;
    private LocalDateTime dateReservation;
    private String statut;
    private String modalitesPaiement;
    private String paysdestination;

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModalitesPaiement() { return modalitesPaiement; }
    public void setModalitesPaiement(String modalitesPaiement) { this.modalitesPaiement = modalitesPaiement; }

    public String getPaysdestination() { return paysdestination; }
    public void setPaysdestination(String paysdestination) { this.paysdestination = paysdestination; }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", clientId=" + clientId +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                ", modalitesPaiement='" + modalitesPaiement + '\'' +
                ", paysdestination='" + paysdestination + '\'' +
                '}';
    }
}
