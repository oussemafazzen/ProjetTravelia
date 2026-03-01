package models;

import models.enums.Role;
import models.enums.Statut;

public abstract class User {
    protected int id;
    protected String email;
    protected String password;
    protected Role role;
    protected Statut statut;
    protected int failed_attempts;
    protected String googleId;
    protected boolean emailConfirmed;

    public User() {
    }

    public User(String email, String password, Role role, Statut statut) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.statut = statut;
    }

    public User(int id, String email, String password, Role role, Statut statut) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public int getFailed_attempts() {
        return failed_attempts;
    }

    public void setFailed_attempts(int failed_attempts) {
        this.failed_attempts = failed_attempts;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                '}';
    }
}
