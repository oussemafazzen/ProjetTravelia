package models;

public class DestinationRecommendation {

    private String pays;
    private double score;
    private String reason;

    public DestinationRecommendation() {}

    public DestinationRecommendation(String pays, double score, String reason) {
        this.pays = pays;
        this.score = score;
        this.reason = reason;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "DestinationRecommendation{" +
                "pays='" + pays + '\'' +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                '}';
    }
}
