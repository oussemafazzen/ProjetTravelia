package utils;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    // Switching to Open-Meteo (FREE, NO KEY REQUIRED)
    // We need latitude and longitude, so we'll pass them from the GeoService
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true";

    public static String getWeather(double lat, double lon) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = String.format(java.util.Locale.US, BASE_URL, lat, lon);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONObject current = json.getJSONObject("current_weather");
                double temp = current.getDouble("temperature");
                // Open-Meteo gives weather codes, let's simplify for the demo
                return String.format("%.1f°C", temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Météo indisponible";
    }
}
