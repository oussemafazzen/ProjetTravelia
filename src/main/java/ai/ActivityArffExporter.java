package ai;

import utils.MyDataBase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Exports inscription_activite + activite data to ARFF format for Weka training.
 * Predicts which activity CATEGORY a user will book next.
 */
public class ActivityArffExporter {

    private final Connection cnx;

    /**
     * Dynamically loaded from the activite table — only real categories.
     */
    private List<String> dbCategories;

    public ActivityArffExporter() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.dbCategories = loadCategoriesFromDb();
    }

    /** Returns the categories that actually exist in the database. */
    public List<String> getDbCategories() {
        return dbCategories;
    }

    private List<String> loadCategoriesFromDb() {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT categorie FROM activite WHERE categorie IS NOT NULL AND categorie <> '' ORDER BY categorie";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cats.add(rs.getString(1));
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
        if (cats.isEmpty()) cats.add("Autre");
        System.out.println("📂 Categories from DB: " + cats);
        return cats;
    }

    public void exportToArff(String outPath) throws Exception {

        List<Row> rows = fetchRows();

        if (rows.isEmpty()) {
            System.out.println("⚠️ No inscription data found. Generating synthetic training data...");
            rows = generateSyntheticRows();
        }

        // Build nominal domains
        Set<String> lieux = new TreeSet<>();
        Set<String> categories = new TreeSet<>();
        for (String c : dbCategories) categories.add(safeNominal(c));

        for (Row r : rows) {
            lieux.add(safeNominal(r.lieu));
            categories.add(safeNominal(r.categorie));
        }
        if (lieux.isEmpty()) lieux.add("unknown");

        // Write ARFF
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outPath))) {
            bw.write("@relation travelia_reco_activite\n");
            bw.write("@attribute month numeric\n");
            bw.write("@attribute price_bucket {cheap,medium,high}\n");
            bw.write("@attribute duration_bucket {short,medium,long}\n");
            bw.write("@attribute lieu {" + joinNominals(lieux) + "}\n");
            bw.write("@attribute last_category {" + joinNominals(categories) + ",none}\n");
            bw.write("@attribute target_category {" + joinNominals(categories) + "}\n");
            bw.write("@data\n");

            for (Row r : rows) {
                int month = r.month;
                String priceBucket = bucketizePrice(r.prix);
                String durationBucket = bucketizeDuration(r.duree);
                String lieu = safeNominal(r.lieu);
                String lastCat = safeNominal(r.lastCategory != null ? r.lastCategory : "none");
                String target = safeNominal(r.categorie);

                bw.write(month + "," + priceBucket + "," + durationBucket + "," 
                         + lieu + "," + lastCat + "," + target + "\n");
            }
        }

        System.out.println("✅ ARFF exported with " + rows.size() + " rows: " + outPath);
    }

    // ================ DB ================

    private List<Row> fetchRows() throws Exception {
        String sql =
            "SELECT ia.id_client, ia.date_activite, a.prix, a.duree, a.lieu, a.categorie " +
            "FROM inscriptionactivite ia " +
            "JOIN activite a ON ia.id_activite = a.id_activite " +
            "WHERE a.categorie IS NOT NULL AND a.categorie <> '' " +
            "ORDER BY ia.id_client, ia.date_activite ASC";

        List<Row> rawRows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int clientId = rs.getInt("id_client");
                java.sql.Date dt = rs.getDate("date_activite");
                int month = dt != null ? dt.toLocalDate().getMonthValue() : LocalDateTime.now().getMonthValue();
                double prix = rs.getDouble("prix");
                int duree = rs.getInt("duree");
                String lieu = rs.getString("lieu");
                String categorie = rs.getString("categorie");

                rawRows.add(new Row(clientId, month, prix, duree, lieu, categorie));
            }
        }

        // Link sequences: for each client, track the previous category
        Map<Integer, String> lastCatMap = new HashMap<>();
        for (Row r : rawRows) {
            r.lastCategory = lastCatMap.get(r.clientId);
            lastCatMap.put(r.clientId, r.categorie);
        }

        return rawRows;
    }

    /**
     * Generate synthetic training data from existing activities when no inscriptions exist.
     * This creates plausible training samples so the model can still learn category patterns.
     */
    private List<Row> generateSyntheticRows() throws Exception {
        String sql = "SELECT prix, duree, lieu, categorie FROM activite WHERE categorie IS NOT NULL AND categorie <> ''";
        List<Row> activities = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double prix = rs.getDouble("prix");
                int duree = rs.getInt("duree");
                String lieu = rs.getString("lieu");
                String categorie = rs.getString("categorie");
                activities.add(new Row(0, 1, prix, duree, lieu, categorie));
            }
        }

        if (activities.isEmpty()) {
            throw new IllegalStateException("No activities found in database. Please insert activities first.");
        }

        List<Row> synthetic = new ArrayList<>();
        Random rng = new Random(42);

        // For each activity, generate multiple synthetic "bookings" across different months
        // and with different "last_category" values to create training variety
        for (Row act : activities) {
            for (int month = 1; month <= 12; month += 3) {
                // Synthetic user who had no previous booking
                Row r1 = new Row(0, month, act.prix, act.duree, act.lieu, act.categorie);
                r1.lastCategory = null;
                synthetic.add(r1);

                // Synthetic user whose last booking was the same category (affinity pattern)
                Row r2 = new Row(0, month + 1 > 12 ? 1 : month + 1, act.prix, act.duree, act.lieu, act.categorie);
                r2.lastCategory = act.categorie;
                synthetic.add(r2);

                // Synthetic user whose last booking was a different category (cross-category pattern)
                String otherCat = dbCategories.get(rng.nextInt(dbCategories.size()));
                Row r3 = new Row(0, month + 2 > 12 ? 2 : month + 2, act.prix, act.duree, act.lieu, act.categorie);
                r3.lastCategory = otherCat;
                synthetic.add(r3);
            }
        }

        System.out.println("📊 Generated " + synthetic.size() + " synthetic training rows from " + activities.size() + " activities.");
        return synthetic;
    }

    // ================ Helpers ================

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

    private String joinNominals(Set<String> values) {
        if (values.isEmpty()) return "unknown";
        return String.join(",", values);
    }

    private static class Row {
        final int clientId;
        final int month;
        final double prix;
        final int duree;
        final String lieu;
        final String categorie;
        String lastCategory;

        Row(int clientId, int month, double prix, int duree, String lieu, String categorie) {
            this.clientId = clientId;
            this.month = month;
            this.prix = prix;
            this.duree = duree;
            this.lieu = lieu;
            this.categorie = categorie;
        }
    }
}
