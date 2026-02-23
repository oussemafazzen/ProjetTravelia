package utils;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeoLocationService {
    // Switching to ip-api.com (FREE, NO KEY REQUIRED for non-commercial)
    private static final String BASE_URL = "http://ip-api.com/json/";

    public static JSONObject getUserLocation() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCountry() {
        JSONObject location = getUserLocation();
        if (location != null && location.has("country")) {
            return location.getString("country");
        }
        return "Tunisie";
    }

    public static String getCity() {
        JSONObject location = getUserLocation();
        if (location != null && location.has("city")) {
            return location.getString("city");
        }
        return "Tunis";
    }

    public static double getLat() {
        JSONObject location = getUserLocation();
        return (location != null && location.has("lat")) ? location.getDouble("lat") : 36.8065;
    }

    public static double getLon() {
        JSONObject location = getUserLocation();
        return (location != null && location.has("lon")) ? location.getDouble("lon") : 10.1815;
    }
}
