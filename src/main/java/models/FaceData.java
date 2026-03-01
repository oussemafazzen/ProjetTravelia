package models;

import java.sql.Timestamp;

public class FaceData {
    private int id;
    private int userId;
    private String faceToken;
    private String faceEncoding;
    private Timestamp createdAt;

    public FaceData() {
    }

    public FaceData(int userId, String faceToken, String faceEncoding) {
        this.userId = userId;
        this.faceToken = faceToken;
        this.faceEncoding = faceEncoding;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFaceToken() { return faceToken; }
    public void setFaceToken(String faceToken) { this.faceToken = faceToken; }

    public String getFaceEncoding() { return faceEncoding; }
    public void setFaceEncoding(String faceEncoding) { this.faceEncoding = faceEncoding; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "FaceData{" +
                "id=" + id +
                ", userId=" + userId +
                ", faceToken='" + faceToken + '\'' +
                '}';
    }
}
