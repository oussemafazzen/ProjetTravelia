package services;

import ai.TraveliaRecommendationAI;
import models.DestinationRecommendation;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RecommendationService {

    private final Connection cnx;
    private final TraveliaRecommendationAI ai = new TraveliaRecommendationAI();

    public RecommendationService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<DestinationRecommendation> recommendForClient(int clientId, int topN) {
        try {
            List<TraveliaRecommendationAI.ScoredLabel> aiResults = ai.recommendPaysTopN(clientId, topN);
            
            List<DestinationRecommendation> out = new ArrayList<>();
            if (!aiResults.isEmpty()) {
                for (TraveliaRecommendationAI.ScoredLabel s : aiResults) {
                    String display = s.label.length() > 0 ? s.label.substring(0, 1).toUpperCase() + s.label.substring(1).toLowerCase() : s.label;
                    String reason = "Prédit par notre IA (RandomForest)";
                    if (s.score == 0.75) reason = "Destination populaire recommandée";
                    else if (s.score == 0.65) reason = "Nouvelle destination à découvrir";
                    
                    out.add(new DestinationRecommendation(
                        display,
                        round2(s.score * 100),
                        reason
                    ));
                }
            }
            return out; // Return whatever AI found, or empty if nothing
            
        } catch (Exception e) {
            System.err.println("AI recommendation failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
