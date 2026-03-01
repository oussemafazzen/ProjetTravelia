package models;

import java.sql.Timestamp;

public class SecurityLog {
    private int id;
    private int userId;
    private String eventType;
    private String ipAddress;
    private String details;
    private Timestamp createdAt;

    public SecurityLog() {
    }

    public SecurityLog(int userId, String eventType, String details) {
        this.userId = userId;
        this.eventType = eventType;
        this.details = details;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SecurityLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventType='" + eventType + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
