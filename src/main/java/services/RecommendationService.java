package services;

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

    public RecommendationService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<DestinationRecommendation> recommendForClient(int clientId, int topN) {

        List<HistoryRow> history = getClientHistoryPays(clientId);
        Map<Integer, Set<String>> all = getAllClientsPays();

        Set<String> alreadyVisited = new HashSet<>();
        for (HistoryRow h : history) {
            if (h.value != null) alreadyVisited.add(h.value);
        }

        Map<String, Double> scores = new HashMap<>();
        LocalDate today = LocalDate.now();

        // A) We STILL calculate frequencies to understand the user's TASTES,
        // but we DO NOT add these visited countries to the final recommendation `scores` map.
        Map<String, Integer> counts = new HashMap<>();
        for (HistoryRow h : history) {
            if (h.value == null) continue;
            String val = h.value.toLowerCase().trim();
            counts.merge(val, 1, Integer::sum);
        }

        // B) Clients similaires (Jaccard) - This is the MAIN engine now
        Set<String> mySet = all.getOrDefault(clientId, Collections.emptySet());

        int K = 10; // Increased neighbor pool since we are restricting options more
        List<Neighbor> neighbors = new ArrayList<>();
        for (Map.Entry<Integer, Set<String>> e : all.entrySet()) {
            int otherId = e.getKey();
            if (otherId == clientId) continue;

            double sim = jaccard(mySet, e.getValue());
            if (sim > 0.0) neighbors.add(new Neighbor(otherId, sim));
        }
        neighbors.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        if (neighbors.size() > K) neighbors = neighbors.subList(0, K);

        Map<String, Double> neighborScores = new HashMap<>();
        for (Neighbor nb : neighbors) {
            for (String pays : all.getOrDefault(nb.clientId, Collections.emptySet())) {
                // EXCRUCIATINGLY IMPORTANT: SKIP if the user has ALREADY been there
                if (alreadyVisited.contains(pays)) continue;
                neighborScores.merge(pays, nb.similarity, Double::sum);
            }
        }

        for (Map.Entry<String, Double> e : neighborScores.entrySet()) {
            scores.merge(e.getKey().toLowerCase().trim(), e.getValue(), Double::sum);
        }

        // C) Fallback global if no similar clients or no new countries found from similar clients
        if (scores.isEmpty()) {
            Map<String, Long> global = new HashMap<>();
            for (Set<String> set : all.values()) {
                for (String p : set) {
                    if (p != null) {
                        String paysLower = p.toLowerCase().trim();
                        // ONLY add to fallback if user hasn't visited it yet
                        if (!alreadyVisited.contains(paysLower)) {
                            global.merge(paysLower, 1L, Long::sum);
                        }
                    }
                }
            }

            // If the database has no other countries the user hasn't visited, use a hardcoded fallback list!
            if (global.isEmpty()) {
                List<String> staticDestinations = Arrays.asList(
                    "Japon", "Italie", "Maldives", "Grèce", "Suisse", 
                    "Brésil", "Canada", "Turquie", "Thaïlande", "Espagne",
                    "Égypte", "Mexique", "Australie", "Indonésie"
                );
                for (String dest : staticDestinations) {
                    if (!alreadyVisited.contains(dest.toLowerCase())) {
                        global.put(dest.toLowerCase(), 1L);
                    }
                }
            }

            List<Map.Entry<String, Long>> g = new ArrayList<>(global.entrySet());
            Collections.shuffle(g);
            g.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            List<DestinationRecommendation> out = new ArrayList<>();
            for (int i = 0; i < Math.min(topN, g.size()); i++) {
                String name = g.get(i).getKey();
                String display = name.length() > 0 ? name.substring(0, 1).toUpperCase() + name.substring(1) : name;
                out.add(new DestinationRecommendation(display, (double)g.get(i).getValue(), "Découverte populaire"));
            }
            return out;
        }

        // D) Rank and format the personalized recommendations
        List<Map.Entry<String, Double>> ranked = new ArrayList<>(scores.entrySet());
        // Shuffle first to avoid stable sorting always picking same top elements if scores tie
        Collections.shuffle(ranked);
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<DestinationRecommendation> out = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, ranked.size()); i++) {
            String name = ranked.get(i).getKey();
            double score = ranked.get(i).getValue();
            
            String display = name.length() > 0 ? name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() : name;
            
            out.add(new DestinationRecommendation(
                    display,
                    round2(score),
                    "Basé sur des voyageurs aux goûts similaires"
            ));
        }
        return out;
    }

    // ===================== JDBC HELPERS =====================

    private static class HistoryRow {
        final LocalDate date;
        final String value;

        HistoryRow(LocalDate date, String value) {
            this.date = date;
            this.value = value;
        }
    }

    private static class Neighbor {
        final int clientId;
        final double similarity;

        Neighbor(int clientId, double similarity) {
            this.clientId = clientId;
            this.similarity = similarity;
        }
    }

    private List<HistoryRow> getClientHistoryPays(int clientId) {
        List<HistoryRow> list = new ArrayList<>();
        String sql = "SELECT date_reservation, paysdestination FROM reservation WHERE id_client=? AND paysdestination IS NOT NULL AND paysdestination <> ''";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate(1);
                    String v = rs.getString(2);
                    if (v != null) list.add(new HistoryRow(d == null ? null : d.toLocalDate(), v.toLowerCase().trim()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Map<Integer, Set<String>> getAllClientsPays() {
        Map<Integer, Set<String>> map = new HashMap<>();
        String sql = "SELECT id_client, paysdestination FROM reservation WHERE paysdestination IS NOT NULL AND paysdestination <> ''";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt(1);
                String v = rs.getString(2);
                if (v == null || v.trim().isEmpty()) continue;
                map.computeIfAbsent(id, k -> new HashSet<>()).add(v.toLowerCase().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private static double jaccard(Set<String> a, Set<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / (double) union.size();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
