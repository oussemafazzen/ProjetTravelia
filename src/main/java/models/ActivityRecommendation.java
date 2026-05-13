package models;

/**
 * Represents a single activity recommendation from the AI engine.
 * Wraps a full Activite object with a confidence score and explanation reason.
 */
public class ActivityRecommendation {

    private Activite activite;
    private double score;
    private String reason;

    public ActivityRecommendation() {}

    public ActivityRecommendation(Activite activite, double score, String reason) {
        this.activite = activite;
        this.score = score;
        this.reason = reason;
    }

    public Activite getActivite() {
        return activite;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
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
        return "ActivityRecommendation{" +
                "activite=" + (activite != null ? activite.getNom() : "null") +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                '}';
    }
}
