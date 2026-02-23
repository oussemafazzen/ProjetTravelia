package models;

import models.enums.Role;
import models.enums.Statut;

public class Administrateur extends User {

    public Administrateur() {
        super();
        this.role = Role.ADMINISTRATEUR;
        this.statut = Statut.ACTIF;
    }

    public Administrateur(String email, String password, Role role, Statut statut) {
        super(email, password, role, statut);
    }

    public Administrateur(int id, String email, String password, Role role, Statut statut) {
        super(id, email, password, role, statut);
    }

    @Override
    public String toString() {
        return "Administrateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
