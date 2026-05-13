package ai;

import models.Activite;
import services.ActiviteService;
import utils.MyDataBase;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Runtime AI engine for activity recommendations.
 * Uses the trained Weka model to predict which activity category a user will prefer,
 * then returns concrete Activite objects from those categories.
 */
public class ActivityRecommendationAI {

    private static final String MODEL_RESOURCE = "/ai-models/travelia-reco-activite-v1.model";

    private final Connection cnx;
    private Classifier model;
    private Instances header;

    public ActivityRecommendationAI() {
        this.cnx = MyDataBase.getInstance().getCnx();
        tryLoad();
    }

    private synchronized void tryLoad() {
        if (model != null && header != null) return;
        try {
            this.model = loadModel();
            this.header = buildHeader();
            System.out.println("✅ Activity AI Model & Header loaded successfully.");
        } catch (Exception e) {
            System.err.println("❌ Activity AI Load Error: " + e.getMessage());
            this.model = null;
            this.header = null;
        }
    }

    /**
     * Main recommendation method.
     * Returns a list of recommended Activite objects with scores.
     */
    public List<ScoredActivite> recommendActivitesTopN(int clientId, int topN) {
        try {
            // Load user's booked activity IDs to exclude
            Set<Integer> bookedIds = loadBookedActivityIds(clientId);
            
            // Load all activities
            ActiviteService activiteService = new ActiviteService();
            List<Activite> allActivites = activiteService.recupToutesActivites();
            
            // If user has no bookings → cold start: return most popular
            if (bookedIds.isEmpty()) {
                System.out.println("DEBUG Activity AI: Cold start for client " + clientId + " — using popularity.");
                return getPopularActivites(allActivites, bookedIds, topN);
            }

            // Warm user: use AI to predict preferred categories
            if (model == null || header == null) {
                tryLoad();
                if (model == null || header == null) {
                    System.out.println("DEBUG Activity AI: Model not available, falling back to popularity.");
                    return getPopularActivites(allActivites, bookedIds, topN);
                }
            }

            // Load context
            Context ctx = loadLastContext(clientId);
            if (ctx == null) {
                return getPopularActivites(allActivites, bookedIds, topN);
            }

            System.out.println("DEBUG Activity AI: Context → month=" + ctx.month 
                + ", price=" + ctx.priceBucket + ", duration=" + ctx.durationBucket 
                + ", lieu=" + ctx.lieu + ", lastCat=" + ctx.lastCategory);

            // Build Weka instance
            DenseInstance inst = new DenseInstance(header.numAttributes());
            inst.setDataset(header);
            inst.setValue(header.attribute("month"), ctx.month);
            inst.setValue(header.attribute("price_bucket"), ctx.priceBucket);
            inst.setValue(header.attribute("duration_bucket"), ctx.durationBucket);
            
            // Handle lieu safely - if value not in nominal list, use first available
            try {
                inst.setValue(header.attribute("lieu"), ctx.lieu);
            } catch (Exception e) {
                inst.setMissing(header.attribute("lieu"));
            }
            
            try {
                inst.setValue(header.attribute("last_category"), ctx.lastCategory);
            } catch (Exception e) {
                inst.setMissing(header.attribute("last_category"));
            }
            
            inst.setMissing(header.classAttribute());

            // Predict category distribution
            double[] dist = model.distributionForInstance(inst);

            // Build ranked list of categories
            List<ScoredCategory> catScores = new ArrayList<>();
            for (int i = 0; i < dist.length; i++) {
                String cat = header.classAttribute().value(i);
                if (dist[i] > 0) {
                    catScores.add(new ScoredCategory(cat, dist[i]));
                }
            }
            catScores.sort((a, b) -> Double.compare(b.score, a.score));

            System.out.println("DEBUG Activity AI: Category predictions: " + catScores);

            // Build result: pick activities from top categories
            List<ScoredActivite> result = new ArrayList<>();
            
            for (ScoredCategory cs : catScores) {
                if (result.size() >= topN) break;
                
                String catDisplay = restoreCategory(cs.category);
                
                // Find activities in this category that user hasn't booked
                List<Activite> candidates = allActivites.stream()
                    .filter(a -> a.getCategorie() != null 
                              && safeNominal(a.getCategorie()).equals(cs.category)
                              && !bookedIds.contains(a.getIdActivite()))
                    .collect(Collectors.toList());

                // Sort by popularity (most inscriptions first)
                Map<Integer, Integer> popMap = loadInscriptionCounts();
                candidates.sort((a, b) -> Integer.compare(
                    popMap.getOrDefault(b.getIdActivite(), 0),
                    popMap.getOrDefault(a.getIdActivite(), 0)
                ));

                for (Activite a : candidates) {
                    if (result.size() >= topN) break;
                    boolean alreadyIn = result.stream().anyMatch(r -> r.activite.getIdActivite() == a.getIdActivite());
                    if (!alreadyIn) {
                        result.add(new ScoredActivite(a, cs.score, "Prédit par notre IA (RandomForest)"));
                        System.out.println("DEBUG Activity AI: Adding AI pick: " + a.getNom() + " (" + catDisplay + ", score=" + cs.score + ")");
                    }
                }
            }

            // Fallback: fill remaining spots with popular activities
            if (result.size() < topN) {
                System.out.println("DEBUG Activity AI: Filling with popular activities (" + result.size() + "/" + topN + ")");
                List<ScoredActivite> popular = getPopularActivites(allActivites, bookedIds, topN);
                for (ScoredActivite p : popular) {
                    if (result.size() >= topN) break;
                    boolean alreadyIn = result.stream().anyMatch(r -> r.activite.getIdActivite() == p.activite.getIdActivite());
                    if (!alreadyIn) {
                        result.add(p);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("Activity AI error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // =================== COLD START: POPULARITY / DIVERSITY ===================

    private List<ScoredActivite> getPopularActivites(List<Activite> allActivites, Set<Integer> bookedIds, int topN) {
        try {
            Map<Integer, Integer> popMap = loadInscriptionCounts();
            
            // Filter out already booked activities
            List<Activite> available = allActivites.stream()
                .filter(a -> !bookedIds.contains(a.getIdActivite()))
                .collect(Collectors.toList());

            // Group by category for diversity
            Map<String, List<Activite>> byCategory = new LinkedHashMap<>();
            for (Activite a : available) {
                String cat = a.getCategorie() != null ? a.getCategorie() : "Autre";
                byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(a);
            }

            // Shuffle within each category
            Random rng = new Random();
            for (List<Activite> list : byCategory.values()) {
                Collections.shuffle(list, rng);
            }

            // Round-robin pick: one from each category to ensure diversity
            List<Activite> diverse = new ArrayList<>();
            List<String> categoryKeys = new ArrayList<>(byCategory.keySet());
            Collections.shuffle(categoryKeys, rng); // Randomize category order too

            int idx = 0;
            while (diverse.size() < topN && idx < categoryKeys.size()) {
                List<Activite> catList = byCategory.get(categoryKeys.get(idx));
                if (!catList.isEmpty()) {
                    diverse.add(catList.remove(0));
                }
                idx++;
            }

            // If still not enough (fewer categories than topN), pick more randomly
            if (diverse.size() < topN) {
                List<Activite> remaining = available.stream()
                    .filter(a -> !diverse.contains(a))
                    .collect(Collectors.toList());
                Collections.shuffle(remaining, rng);
                for (Activite a : remaining) {
                    if (diverse.size() >= topN) break;
                    diverse.add(a);
                }
            }

            List<ScoredActivite> result = new ArrayList<>();
            for (Activite a : diverse) {
                int count = popMap.getOrDefault(a.getIdActivite(), 0);
                double score = count > 0 ? 0.80 : 0.65;
                String reason = count > 0 ? "Activité populaire (" + count + " inscriptions)" : "Activité recommandée";
                result.add(new ScoredActivite(a, score, reason));
            }
            return result;

        } catch (Exception e) {
            System.err.println("Error loading popular activities: " + e.getMessage());
            return List.of();
        }
    }

    // =================== MODEL + HEADER ===================

    private Classifier loadModel() {
        // 1. Try classpath resource
        try (InputStream is = getClass().getResourceAsStream(MODEL_RESOURCE)) {
            if (is != null) {
                return (Classifier) SerializationHelper.read(is);
            }
        } catch (Exception ignored) {}

        // 2. Try filesystem (development)
        java.io.File file = new java.io.File("src/main/resources" + MODEL_RESOURCE);
        if (file.exists()) {
            try {
                return (Classifier) SerializationHelper.read(file.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Found model file but failed to read it: " + e.getMessage(), e);
            }
        }

        throw new IllegalStateException("Activity AI Model not found. Please run TrainActivityRecoModel first.");
    }

    private Instances buildHeader() {
        try {
            // Fetch all distinct lieux from DB
            Set<String> lieux = new TreeSet<>();
            String sql = "SELECT DISTINCT lieu FROM activite WHERE lieu IS NOT NULL AND lieu <> ''";
            try (PreparedStatement ps = cnx.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lieux.add(safeNominal(rs.getString(1)));
            }
            if (lieux.isEmpty()) lieux.add("unknown");

            // Categories
            Set<String> categories = new TreeSet<>();
            ActivityArffExporter exporter = new ActivityArffExporter();
            for (String c : exporter.getDbCategories()) categories.add(safeNominal(c));
            String sqlCat = "SELECT DISTINCT categorie FROM activite WHERE categorie IS NOT NULL AND categorie <> ''";
            try (PreparedStatement ps = cnx.prepareStatement(sqlCat);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) categories.add(safeNominal(rs.getString(1)));
            }

            // Build attributes
            ArrayList<Attribute> attrs = new ArrayList<>();
            attrs.add(new Attribute("month"));  // numeric
            attrs.add(new Attribute("price_bucket", Arrays.asList("cheap", "medium", "high")));
            attrs.add(new Attribute("duration_bucket", Arrays.asList("short", "medium", "long")));
            attrs.add(new Attribute("lieu", new ArrayList<>(lieux)));

            List<String> catWithNone = new ArrayList<>(categories);
            catWithNone.add("none");
            attrs.add(new Attribute("last_category", catWithNone));
            attrs.add(new Attribute("target_category", new ArrayList<>(categories)));

            Instances h = new Instances("travelia_reco_activite_runtime", attrs, 0);
            h.setClassIndex(h.numAttributes() - 1);
            return h;

        } catch (Exception e) {
            throw new RuntimeException("Failed to build activity header: " + e.getMessage(), e);
        }
    }

    // =================== CONTEXT ===================

    private Context loadLastContext(int clientId) throws Exception {
        String sql =
            "SELECT ia.date_activite, a.prix, a.duree, a.lieu, a.categorie " +
            "FROM inscriptionactivite ia " +
            "JOIN activite a ON ia.id_activite = a.id_activite " +
            "WHERE ia.id_client = ? AND ia.date_activite IS NOT NULL " +
            "ORDER BY ia.date_activite DESC LIMIT 1";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                java.sql.Date dt = rs.getDate("date_activite");
                int month = dt != null ? dt.toLocalDate().getMonthValue() : java.time.LocalDateTime.now().getMonthValue();
                double prix = rs.getDouble("prix");
                int duree = rs.getInt("duree");
                String lieu = safeNominal(rs.getString("lieu"));
                String categorie = safeNominal(rs.getString("categorie"));

                return new Context(month, bucketizePrice(prix), bucketizeDuration(duree), lieu, categorie);
            }
        }
    }

    private Set<Integer> loadBookedActivityIds(int clientId) throws Exception {
        String sql = "SELECT DISTINCT id_activite FROM inscriptionactivite WHERE id_client = ?";
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private Map<Integer, Integer> loadInscriptionCounts() throws Exception {
        String sql = "SELECT id_activite, COUNT(*) as cnt FROM inscriptionactivite GROUP BY id_activite";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("id_activite"), rs.getInt("cnt"));
            }
        }
        return map;
    }

    // =================== HELPERS ===================

    private String bucketizePrice(double prix) {
        if (prix <= 50) return "cheap";
        if (prix <= 120) return "medium";
        return "high";
    }

    private String bucketizeDuration(int duree) {
        if (duree <= 3) return "short";
        if (duree <= 6) return "medium";
        return "long";
    }

    private String safeNominal(String s) {
        if (s == null) return "unknown";
        s = s.trim();
        if (s.isEmpty()) return "unknown";
        s = s.replace(' ', '_')
             .replace(',', '_')
             .replace('{', '_')
             .replace('}', '_')
             .replace('\'', '_');
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Restore a safe-nominal category back to display format.
     */
    private String restoreCategory(String safe) {
        if (safe == null || safe.isEmpty()) return safe;
        // Capitalize first letter
        return safe.substring(0, 1).toUpperCase() + safe.substring(1).replace('_', ' ');
    }

    // =================== INNER CLASSES ===================

    private static class Context {
        final int month;
        final String priceBucket;
        final String durationBucket;
        final String lieu;
        final String lastCategory;

        Context(int month, String priceBucket, String durationBucket, String lieu, String lastCategory) {
            this.month = month;
            this.priceBucket = priceBucket;
            this.durationBucket = durationBucket;
            this.lieu = lieu;
            this.lastCategory = lastCategory;
        }
    }

    private static class ScoredCategory {
        final String category;
        final double score;

        ScoredCategory(String category, double score) {
            this.category = category;
            this.score = score;
        }

        @Override
        public String toString() {
            return category + " (" + String.format(Locale.ROOT, "%.3f", score) + ")";
        }
    }

    public static class ScoredActivite {
        public final Activite activite;
        public final double score;
        public final String reason;

        public ScoredActivite(Activite activite, double score, String reason) {
            this.activite = activite;
            this.score = score;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return activite.getNom() + " [" + activite.getCategorie() + "] (" 
                 + String.format(Locale.ROOT, "%.3f", score) + ") — " + reason;
        }
    }
}
