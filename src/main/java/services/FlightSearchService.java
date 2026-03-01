package services;

import models.Billet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Robust Flight Search Service using org.json (Verified in pom.xml)
 * Handles the actual nested structure of SerpApi Google Flights results.
 */
public class FlightSearchService {

    private static final String API_KEY = "01455e5e540f83bd3a8e2da3a43e570366ae82afc71fadd9c83e414f824af06c";
    private static final String ENGINE = "google_flights";
    
    private static final Map<String, String> IATA_MAP = new HashMap<>();

    static {
        IATA_MAP.put("tunis", "TUN");
        IATA_MAP.put("tunisie", "TUN");
        IATA_MAP.put("paris", "CDG");
        IATA_MAP.put("par", "CDG"); // Force PAR to CDG for reliability
        IATA_MAP.put("france", "CDG");
        IATA_MAP.put("italie", "FCO");
        IATA_MAP.put("rome", "FCO");
        IATA_MAP.put("russia", "SVO");
        IATA_MAP.put("russie", "SVO");
        IATA_MAP.put("allemagne", "FRA");
        IATA_MAP.put("germany", "FRA");
        IATA_MAP.put("espagne", "MAD");
        IATA_MAP.put("spain", "MAD");
    }

    private String getIataCode(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) return "CDG";
        String cleanName = locationName.toLowerCase().trim();

        // 1. Check direct map first (e.g. "paris" -> "CDG")
        if (IATA_MAP.containsKey(cleanName)) return IATA_MAP.get(cleanName);

        // 2. Try to extract from parentheses (e.g. "Tunis (TUN)" -> "TUN")
        if (cleanName.contains("(") && cleanName.contains(")")) {
            String extracted = cleanName.substring(cleanName.indexOf("(") + 1, cleanName.indexOf(")")).trim().toUpperCase();
            // If the extracted code is "PAR", force it to "CDG" for SerpApi robustness
            if (extracted.equals("PAR")) return "CDG";
            if (extracted.length() == 3) return extracted;
        }

        // 3. Last fallback
        if (cleanName.startsWith("vers ")) {
            String sub = cleanName.substring(5).trim();
            if (IATA_MAP.containsKey(sub)) return IATA_MAP.get(sub);
        }

        return IATA_MAP.getOrDefault(cleanName, "CDG");
    }

    public List<Billet> searchRealFlights(String fromLocation, String toLocation, String dateStr) {
        List<Billet> results = new ArrayList<>();
        try {
            String depIata = getIataCode(fromLocation);
            String arrIata = getIataCode(toLocation);
            String formattedDate = dateStr;
            try {
                // If date comes as dd/MM/yyyy from the picker
                if (dateStr.contains("/")) {
                    LocalDate d = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    formattedDate = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                // Fallback to today if parsing fails
                formattedDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            String urlString = String.format(
                    "https://serpapi.com/search.json?api_key=%s&engine=%s&departure_id=%s&arrival_id=%s&outbound_date=%s&type=2&currency=EUR&hl=fr",
                    API_KEY, ENGINE, depIata, arrIata, formattedDate
            );

            System.out.println("--- API CALL ---");
            System.out.println("Route: " + depIata + " -> " + arrIata + " on " + formattedDate);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                String rawJson = response.toString();
                JSONObject data = new JSONObject(rawJson);
                
                System.out.println("API Status: " + (data.has("search_metadata") ? data.getJSONObject("search_metadata").getString("status") : "OK"));

                if (data.has("best_flights")) {
                    parseFlightsArray(data.getJSONArray("best_flights"), results);
                }
                
                if (results.size() < 3 && data.has("other_flights")) {
                    System.out.println("Looking into other_flights...");
                    parseFlightsArray(data.getJSONArray("other_flights"), results);
                }
                
                if (results.isEmpty()) {
                    System.out.println("WARNING: API returned no flights. Checking for errors in response...");
                    if (data.has("error")) System.out.println("API Error Message: " + data.getString("error"));
                    // Log a bit of the JSON to help debug
                    System.out.println("Raw response start: " + (rawJson.length() > 200 ? rawJson.substring(0, 200) : rawJson));
                }

                System.out.println("SUCCESS: Found " + results.size() + " real flights.");
            } else {
                System.out.println("ERROR: API returned HTTP code " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("API Search Logic Error: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    private void parseFlightsArray(JSONArray flightsArray, List<Billet> results) {
        // SerpApi time format is "yyyy-MM-dd HH:mm"
        DateTimeFormatter serpFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (int i = 0; i < flightsArray.length(); i++) {
            try {
                JSONObject flightBlock = flightsArray.getJSONObject(i);
                JSONArray legs = flightBlock.getJSONArray("flights");
                if (legs.length() == 0) continue;

                // For simplicity, we take the first leg of the connection
                JSONObject firstLeg = legs.getJSONObject(0);
                JSONObject lastLeg = legs.getJSONObject(legs.length() - 1);
                
                String airline = firstLeg.optString("airline", "Indéterminée");
                String flightNo = firstLeg.optString("flight_number", "FL-" + (1000 + i));
                double price = flightBlock.optDouble("price", 0.0);

                // Times are inside the departure/arrival_airport objects
                String depTimeStr = firstLeg.getJSONObject("departure_airport").getString("time");
                String arrTimeStr = lastLeg.getJSONObject("arrival_airport").getString("time");

                LocalDateTime dep = LocalDateTime.parse(depTimeStr, serpFormat);
                LocalDateTime arr = LocalDateTime.parse(arrTimeStr, serpFormat);

                Billet b = new Billet();
                b.setTypeTransport("avion");
                String airlineCode = airline.length() >= 2 ? airline.substring(0, 2).toUpperCase() : "FL";
                // Keep it short for now to stay under 20 chars, but use the '|' delimiter the controller expects
                b.setNumeroBillet(airlineCode + "|" + flightNo);
                b.setPrix(price);
                b.setDateDepart(dep);
                b.setDateArrivee(arr);
                b.setStatut("disponible");
                results.add(b);
                
                if (results.size() >= 10) break;
            } catch (Exception e) {
                System.out.println("Could not parse individual flight entry: " + e.getMessage());
            }
        }
    }
}
