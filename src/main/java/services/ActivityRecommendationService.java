package services;

import ai.ActivityRecommendationAI;
import models.ActivityRecommendation;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for activity recommendations.
 * Connects the AI engine to the UI controllers.
 */
public class ActivityRecommendationService {

    private final ActivityRecommendationAI ai = new ActivityRecommendationAI();

    public List<ActivityRecommendation> recommendForClient(int clientId, int topN) {
        try {
            List<ActivityRecommendationAI.ScoredActivite> aiResults = ai.recommendActivitesTopN(clientId, topN);

            List<ActivityRecommendation> out = new ArrayList<>();
            for (ActivityRecommendationAI.ScoredActivite s : aiResults) {
                out.add(new ActivityRecommendation(
                    s.activite,
                    round2(s.score * 100),
                    s.reason
                ));
            }
            return out;

        } catch (Exception e) {
            System.err.println("Activity AI recommendation failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
