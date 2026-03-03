package ai;

import utils.MyDataBase;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class TraveliaRecommendationAI {

    private static final String MODEL_RESOURCE = "/ai-models/travelia-reco-pays-v1.model";

    public static final List<String> WORLD_COUNTRIES = TraveliaArffExporter.WORLD_COUNTRIES;

    private final Connection cnx;
    private Classifier model;

    // domain values (nominals) doivent matcher training
    // on les recharge depuis DB à runtime pour construire l'Instances "header"
    private Instances header;

    public TraveliaRecommendationAI() {
        this.cnx = MyDataBase.getInstance().getCnx();
        tryLoad();
    }

    private synchronized void tryLoad() {
        if (model != null && header != null) return;
        try {
            this.model = loadModel();
            this.header = buildHeaderFromDb();
            System.out.println("✅ AI Model & Header loaded successfully.");
        } catch (Exception e) {
            System.err.println("❌ AI Load Error: " + e.getMessage());
            this.model = null;
            this.header = null;
        }
    }

    public List<ScoredLabel> recommendPaysTopN(int clientId, int topN) {
        if (model == null || header == null) {
            tryLoad();
            if (model == null || header == null) return List.of();
        }
        
        try {
            // contexte = dernière réservation du client (payment + transport + prix) + month
            Context ctx = loadLastContext(clientId);
            if (ctx == null) return List.of();
            
            System.out.println("DEBUG AI: Context loaded -> month=" + ctx.month + ", transport=" + ctx.transport + ", price=" + ctx.maxPrix + ", lastC=" + ctx.lastCountry);
            DenseInstance inst = new DenseInstance(header.numAttributes());
            inst.setDataset(header);

            inst.setValue(header.attribute("month"), ctx.month);
            inst.setValue(header.attribute("payment"), safeNominal(ctx.payment));
            inst.setValue(header.attribute("transport"), safeNominal(ctx.transport));
            inst.setValue(header.attribute("price_bucket"), bucketize(ctx.maxPrix));
            inst.setValue(header.attribute("last_country"), safeNominal(ctx.lastCountry != null ? ctx.lastCountry : "none"));

            // class (target_pays) = missing
            inst.setMissing(header.classAttribute());

            System.out.println("DEBUG AI: Instance to predict: " + inst);
            double[] dist = model.distributionForInstance(inst);

            List<ScoredLabel> list = new ArrayList<>();
            for (int i = 0; i < dist.length; i++) {
                String label = header.classAttribute().value(i);
                list.add(new ScoredLabel(label, dist[i]));
            }

            System.out.println("DEBUG AI: Prediction raw results count: " + list.size());
            list.sort((a, b) -> Double.compare(b.score, a.score));
            System.out.println("DEBUG AI: Top 5 raw predictions: " + list.subList(0, Math.min(5, list.size())));

            // exclure pays déjà visités ET labels internes
            Set<String> visited = loadVisitedPays(clientId);
            List<String> internalLabels = Arrays.asList("none", "unknown", "none_", "unknown_", "null");
            System.out.println("DEBUG AI: Visited countries for client " + clientId + ": " + visited);

            List<ScoredLabel> out = new ArrayList<>();
            for (ScoredLabel s : list) {
                String labLow = s.label.toLowerCase().trim();
                if (internalLabels.contains(labLow)) continue;
                if (visited.contains(labLow)) continue;
                if (s.score <= 0.0) continue; // Only take AI suggestions if they have an actual probability > 0
                
                System.out.println("DEBUG AI: Adding AI suggestion: " + s.label + " (" + s.score + ")");
                out.add(s);
                if (out.size() >= topN) break;
            }

            // Fallback: si on n'a pas assez de recommandations unvisited de l'IA, on complète par de la popularité globale
            if (out.size() < topN) {
                System.out.println("DEBUG AI: Adding global popular fallback to fill up to " + topN + " (currently have " + out.size() + ")");
                List<String> popular = loadPopularGlobalPays(clientId, 20);
                for (String p : popular) {
                    if (out.size() >= topN) break;
                    String pLow = p.toLowerCase().trim();
                    if (internalLabels.contains(pLow)) continue;
                    
                    boolean alreadyIn = false;
                    for(ScoredLabel existing : out) if(existing.label.equalsIgnoreCase(p)) alreadyIn = true;
                    
                    if (!alreadyIn && !visited.contains(pLow)) {
                        System.out.println("DEBUG AI: Adding popularity fallback: " + p);
                        out.add(new ScoredLabel(p, 0.75)); // score fictif de 75%
                    } else {
                        System.out.println("DEBUG AI: Popularity fallback skipped: " + p + " (alreadyIn=" + alreadyIn + ", visited=" + visited.contains(pLow) + ")");
                    }
                }
            } else {
                System.out.println("DEBUG AI: No popularity fallback needed (out.size=" + out.size() + ")");
            }

            // Fallback 2: Si toujours pas assez, on pioche dans le "Monde" au hasard
            if (out.size() < topN) {
                System.out.println("DEBUG AI: Adding world discovery fallback (currently have " + out.size() + ")");
                List<String> world = new ArrayList<>(WORLD_COUNTRIES);
                Collections.shuffle(world); 
                for (String w : world) {
                    if (out.size() >= topN) break;
                    String wLow = w.toLowerCase().trim();
                    if (internalLabels.contains(wLow)) continue;

                    boolean alreadyIn = false;
                    for(ScoredLabel existing : out) if(existing.label.equalsIgnoreCase(w)) alreadyIn = true;

                    if (!alreadyIn && !visited.contains(wLow)) {
                        System.out.println("DEBUG AI: Adding discovery suggestion: " + w);
                        out.add(new ScoredLabel(w, 0.65)); // 65% score for discovery to look better
                    }
                }
            }

            System.out.println("DEBUG AI: Final recommendations to return: " + out);
            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // =======================
    // MODEL + HEADER
    // =======================

    private Classifier loadModel() {
        // 1. Try classpath resource (standard for JAR/production)
        try (InputStream is = getClass().getResourceAsStream(MODEL_RESOURCE)) {
            if (is != null) {
                return (Classifier) weka.core.SerializationHelper.read(is);
            }
        } catch (Exception ignored) {}

        // 2. Try filesystem (for development/immediate use after training)
        // Check relative to current working directory (usually project root)
        java.io.File file = new java.io.File("src/main/resources" + MODEL_RESOURCE);
        if (file.exists()) {
            try {
                return (Classifier) weka.core.SerializationHelper.read(file.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Found model file but failed to read it: " + e.getMessage(), e);
            }
        }

        throw new IllegalStateException("AI Model not found. Please run TrainTraveliaRecoModel first.");
    }

    private Instances buildHeaderFromDb() {
        try {
            Set<String> payments = fetchNominals("reservation", "modalites_paiement");
            Set<String> transports = fetchNominals("billet", "type_transport");
            Set<String> countries = fetchNominals("reservation", "paysdestination");
            countries.add("none");
            for (String c : WORLD_COUNTRIES) {
                countries.add(safeNominal(c));
            }

            if (payments.isEmpty()) payments.add("unknown");
            if (transports.isEmpty()) transports.add("unknown");
            if (countries.isEmpty()) countries.add("unknown");

            ArrayList<Attribute> attrs = new ArrayList<>();
            attrs.add(new Attribute("month"));     // numeric
            attrs.add(new Attribute("payment", new ArrayList<>(payments)));
            attrs.add(new Attribute("transport", new ArrayList<>(transports)));
            attrs.add(new Attribute("price_bucket", Arrays.asList("cheap","medium","high")));
            attrs.add(new Attribute("last_country", new ArrayList<>(countries)));
            attrs.add(new Attribute("target_pays", new ArrayList<>(countries))); // class

            Instances h = new Instances("travelia_reco_runtime", attrs, 0);
            h.setClassIndex(h.numAttributes() - 1);
            return h;

        } catch (Exception e) {
            throw new RuntimeException("Failed to build header: " + e.getMessage(), e);
        }
    }

    private Set<String> fetchNominals(String table, String col) throws Exception {
        String sql = "SELECT DISTINCT " + col + " FROM " + table + " WHERE " + col + " IS NOT NULL AND " + col + " <> ''";
        Set<String> set = new TreeSet<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                set.add(safeNominal(rs.getString(1)));
            }
        }
        return set;
    }

    // =======================
    // CONTEXT (last reservation)
    // =======================

    private Context loadLastContext(int clientId) throws Exception {

        // dernière réservation du client
        String sqlRes = 
                "SELECT id_reservation, date_reservation, modalites_paiement, paysdestination " + 
                "FROM reservation WHERE id_client=? AND date_reservation IS NOT NULL " + 
                "ORDER BY date_reservation DESC LIMIT 1";

        Integer idReservation = null;
        LocalDateTime dt = null;
        String payment = "unknown";
        String paysDest = "none";

        try (PreparedStatement ps = cnx.prepareStatement(sqlRes)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Cold start: No reservations yet. Return current month + defaults
                    return new Context(LocalDateTime.now().getMonthValue(), "unknown", "unknown", 0, "none");
                }

                idReservation = rs.getInt("id_reservation");
                Timestamp ts = rs.getTimestamp("date_reservation");
                if (ts != null) dt = ts.toLocalDateTime();
                payment = rs.getString("modalites_paiement");
                paysDest = rs.getString("paysdestination");
            }
        }

        if (dt == null) dt = LocalDateTime.now();

        // billet le plus cher de cette réservation pour obtenir transport + prix
        String sqlBil =
                "SELECT type_transport, prix " +
                "FROM billet WHERE id_reservation=? " +
                "ORDER BY prix DESC LIMIT 1";

        String transport = "unknown";
        double maxPrix = 0;

        try (PreparedStatement ps = cnx.prepareStatement(sqlBil)) {
            ps.setInt(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    transport = rs.getString("type_transport");
                    maxPrix = rs.getDouble("prix");
                }
            }
        }

        return new Context(dt.getMonthValue(), payment, transport, maxPrix, paysDest);
    }

    private List<String> loadPopularGlobalPays(int clientId, int limit) throws Exception {
        String sql = "SELECT paysdestination, COUNT(*) as cnt " +
                     "FROM reservation " +
                     "WHERE paysdestination IS NOT NULL AND paysdestination <> '' " +
                     "AND paysdestination NOT IN ('none', 'unknown') " +
                     "GROUP BY paysdestination ORDER BY cnt DESC LIMIT ?";
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("paysdestination"));
                }
            }
        }
        return results;
    }

    private Set<String> loadVisitedPays(int clientId) throws Exception {
        String sql = "SELECT DISTINCT paysdestination FROM reservation WHERE id_client=? AND paysdestination IS NOT NULL AND paysdestination <> ''";
        Set<String> set = new HashSet<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(safeNominal(rs.getString(1)));
            }
        }
        return set;
    }

    // =======================
    // Helpers
    // =======================

    private String bucketize(double prix) {
        if (prix <= 250) return "cheap";
        if (prix <= 600) return "medium";
        return "high";
    }

    private String safeNominal(String s) {
        if (s == null) return "unknown";
        s = s.trim();
        if (s.isEmpty()) return "unknown";
        s = s.replace(' ', '_')
             .replace(',', '_')
             .replace('{', '_')
             .replace('}', '_');
        return s.toLowerCase(Locale.ROOT);
    }

    private static class Context {
        final int month;
        final String payment;
        final String transport;
        final double maxPrix;
        final String lastCountry;

        Context(int month, String payment, String transport, double maxPrix, String lastCountry) {
            this.month = month;
            this.payment = payment;
            this.transport = transport;
            this.maxPrix = maxPrix;
            this.lastCountry = lastCountry;
        }
    }

    public static class ScoredLabel {
        public final String label;
        public final double score;

        public ScoredLabel(String label, double score) {
            this.label = label;
            this.score = score;
        }

        @Override
        public String toString() {
            return label + " (" + String.format(Locale.ROOT, "%.3f", score) + ")";
        }
    }
}
