package models;

import java.sql.Timestamp;

public class PasswordResetToken {
    private int id;
    private int userId;
    private String token;
    private Timestamp expiryDate;
    private boolean used;
    private Timestamp createdAt;

    public PasswordResetToken() {
    }

    public PasswordResetToken(int userId, String token, Timestamp expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.used = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Timestamp getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Timestamp expiryDate) { this.expiryDate = expiryDate; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return expiryDate.before(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", token='" + token + '\'' +
                ", expiryDate=" + expiryDate +
                ", used=" + used +
                '}';
    }
}
