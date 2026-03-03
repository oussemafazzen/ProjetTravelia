package ai;

import utils.MyDataBase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TraveliaArffExporter {

    private final Connection cnx;

    public static final List<String> WORLD_COUNTRIES = Arrays.asList(
        "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Argentina", "Armenia", "Australia", 
        "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", 
        "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia_and_Herzegovina", "Botswana", "Brazil", "Brunei", 
        "Bulgaria", "Burkina_Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Chile", "China", 
        "Colombia", "Comoros", "Congo", "Costa_Rica", "Croatia", "Cuba", "Cyprus", "Czech_Republic", 
        "Denmark", "Djibouti", "Dominica", "Dominican_Republic", "Ecuador", "Egypt", "El_Salvador", 
        "Equatorial_Guinea", "Eritrea", "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland", "France", 
        "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", 
        "Guyana", "Haiti", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", 
        "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", 
        "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", 
        "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", 
        "Marshall_Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", 
        "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", 
        "Netherlands", "New_Zealand", "Nicaragua", "Niger", "Nigeria", "North_Korea", "North_Macedonia", 
        "Norway", "Oman", "Pakistan", "Palau", "Palestine", "Panama", "Papua_New_Guinea", "Paraguay", 
        "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Samoa", 
        "San_Marino", "Saudi_Arabia", "Senegal", "Serbia", "Seychelles", "Sierra_Leone", "Singapore", 
        "Slovakia", "Slovenia", "Solomon_Islands", "Somalia", "South_Africa", "South_Korea", "South_Sudan", 
        "Spain", "Sri_Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", 
        "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad_and_Tobago", "Tunisia", "Turkey", 
        "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United_Arab_Emirates", "United_Kingdom", 
        "United_States", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican_City", "Venezuela", "Vietnam", 
        "Yemen", "Zambia", "Zimbabwe"
    );

    public TraveliaArffExporter() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // Export un dataset ARFF pour entraîner un modèle qui prédit le pays
    public void exportToArff(String outPath) throws Exception {

        // 1) lire les données depuis DB
        List<Row> rows = fetchRows();

        if (rows.isEmpty()) {
            throw new IllegalStateException("Aucune donnée trouvée pour entraîner (reservation+billet).");
        }

        // 2) construire les domaines nominal (valeurs possibles)
        Set<String> payments = new TreeSet<>();
        Set<String> transports = new TreeSet<>();
        Set<String> countries = new TreeSet<>();
        countries.add("none"); // pour le premier voyage
        
        // Ajouter tous les pays du monde au domaine nominal
        for (String c : WORLD_COUNTRIES) {
            countries.add(safeNominal(c));
        }

        for (Row r : rows) {
            payments.add(safeNominal(r.payment));
            transports.add(safeNominal(r.transport));
            countries.add(safeNominal(r.pays));
        }

        // 3) écrire ARFF
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outPath))) {

            bw.write("@relation travelia_reco_pays\n");

            bw.write("@attribute month numeric\n");
            bw.write("@attribute payment {" + joinNominals(payments) + "}\n");
            bw.write("@attribute transport {" + joinNominals(transports) + "}\n");
            bw.write("@attribute price_bucket {cheap,medium,high}\n");
            bw.write("@attribute last_country {" + joinNominals(countries) + "}\n");

            bw.write("@attribute target_pays {" + joinNominals(countries) + "}\n");
            bw.write("@data\n");

            for (Row r : rows) {
                int month = r.dateReservation.getMonthValue();
                String payment = safeNominal(r.payment);
                String transport = safeNominal(r.transport);
                String bucket = bucketize(r.maxPrix);
                String lastC = safeNominal(r.lastCountry != null ? r.lastCountry : "none");
                String target = safeNominal(r.pays);

                bw.write(month + "," + payment + "," + transport + "," + bucket + "," + lastC + "," + target + "\n");
            }
        }
    }

    // ---------------- DB ----------------

    private List<Row> fetchRows() throws Exception {
        String sql =
                "SELECT r.id_client, r.date_reservation, r.modalites_paiement, r.paysdestination, " +
                "       COALESCE(b.type_transport, 'unknown') AS type_transport, " +
                "       COALESCE(b.prix, 0) AS prix " +
                "FROM reservation r " +
                "LEFT JOIN billet b ON b.id_reservation = r.id_reservation " +
                "WHERE r.paysdestination IS NOT NULL AND r.paysdestination <> '' AND r.date_reservation IS NOT NULL " +
                "ORDER BY r.id_client, r.date_reservation ASC";

        List<Row> rawRows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int clientId = rs.getInt("id_client");
                Timestamp ts = rs.getTimestamp("date_reservation");
                String payment = rs.getString("modalites_paiement");
                String pays = rs.getString("paysdestination");
                String transport = rs.getString("type_transport");
                double prix = rs.getDouble("prix");

                LocalDateTime dt = ts.toLocalDateTime();
                rawRows.add(new Row(clientId, dt, payment, pays, transport, prix));
            }
        }

        // Consolidation par réservation (on garde le billet le plus cher)
        Map<String, Row> uniqueRes = new LinkedHashMap<>();
        for (Row r : rawRows) {
            String key = r.clientId + "|" + r.dateReservation.toString();
            Row existing = uniqueRes.get(key);
            if (existing == null || r.maxPrix > existing.maxPrix) {
                uniqueRes.put(key, r);
            }
        }

        // Lier les séquences de pays par client
        List<Row> consolidated = new ArrayList<>(uniqueRes.values());
        Map<Integer, String> lastCountryMap = new HashMap<>();
        
        for (Row r : consolidated) {
            r.lastCountry = lastCountryMap.get(r.clientId);
            lastCountryMap.put(r.clientId, r.pays);
        }

        return consolidated;
    }

    // ---------------- helpers ----------------

    private String bucketize(double prix) {
        if (prix <= 250) return "cheap";
        if (prix <= 600) return "medium";
        return "high";
    }

    private String safeNominal(String s) {
        if (s == null) return "unknown";
        s = s.trim();
        if (s.isEmpty()) return "unknown";

        // ARFF nominal: pas d'espaces, pas de virgules
        s = s.replace(' ', '_')
             .replace(',', '_')
             .replace('{', '_')
             .replace('}', '_');

        return s.toLowerCase(Locale.ROOT);
    }

    private String joinNominals(Set<String> values) {
        if (values.isEmpty()) return "unknown";
        return String.join(",", values);
    }

    private static class Row {
        final int clientId;
        final LocalDateTime dateReservation;
        final String payment;
        final String pays;
        final String transport;
        final double maxPrix;
        String lastCountry; // Nouveau feature pour la séquence

        Row(int clientId, LocalDateTime dateReservation, String payment, String pays, String transport, double maxPrix) {
            this.clientId = clientId;
            this.dateReservation = dateReservation;
            this.payment = payment;
            this.pays = pays;
            this.transport = transport;
            this.maxPrix = maxPrix;
        }
    }
}

