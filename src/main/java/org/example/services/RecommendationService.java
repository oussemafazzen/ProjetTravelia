package org.example.services;

import org.example.models.DestinationRecommendation;
import org.example.utils.MyDataBase;

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

        // A) fréquence + récence
        for (HistoryRow h : history) {
            if (h.value == null) continue;

            double recencyBoost = 0.0;
            if (h.date != null) {
                long days = ChronoUnit.DAYS.between(h.date, today);
                recencyBoost = 1.0 / (1.0 + Math.max(0, days));
            }
            double base = 1.0 + recencyBoost;
            scores.merge(h.value, base, Double::sum);
        }

        // B) clients similaires (Jaccard)
        Set<String> mySet = all.getOrDefault(clientId, Collections.emptySet());

        int K = 5;
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
                if (alreadyVisited.contains(pays)) continue;
                neighborScores.merge(pays, 2.0 * nb.similarity, Double::sum);
            }
        }

        for (Map.Entry<String, Double> e : neighborScores.entrySet()) {
            scores.merge(e.getKey(), e.getValue(), Double::sum);
        }

        scores.keySet().removeIf(alreadyVisited::contains);

        // fallback global
        if (scores.isEmpty()) {
            Map<String, Long> global = new HashMap<>();
            for (Set<String> set : all.values()) {
                for (String p : set) global.merge(p, 1L, Long::sum);
            }

            List<Map.Entry<String, Long>> g = new ArrayList<>(global.entrySet());
            g.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            List<DestinationRecommendation> out = new ArrayList<>();
            for (int i = 0; i < Math.min(topN, g.size()); i++) {
                out.add(new DestinationRecommendation(g.get(i).getKey(), g.get(i).getValue(), "Populaire globalement"));
            }
            return out;
        }

        List<Map.Entry<String, Double>> ranked = new ArrayList<>(scores.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<DestinationRecommendation> out = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, ranked.size()); i++) {
            String pays = ranked.get(i).getKey();
            double score = ranked.get(i).getValue();
            out.add(new DestinationRecommendation(
                    pays,
                    round2(score),
                    neighborScores.containsKey(pays) ? "Clients similaires" : "Basé sur ton historique"
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
        String sql = "SELECT date_reservation, pays FROM reservation WHERE id_client=? AND pays IS NOT NULL AND pays <> ''";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate(1);
                    String v = rs.getString(2);
                    list.add(new HistoryRow(d == null ? null : d.toLocalDate(), v));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Map<Integer, Set<String>> getAllClientsPays() {
        Map<Integer, Set<String>> map = new HashMap<>();
        String sql = "SELECT id_client, pays FROM reservation WHERE pays IS NOT NULL AND pays <> ''";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt(1);
                String v = rs.getString(2);
                if (v == null) continue;
                map.computeIfAbsent(id, k -> new HashSet<>()).add(v);
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