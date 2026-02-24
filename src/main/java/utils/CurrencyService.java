package utils;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {
    // Switching to ExchangeRate-API (Open Access - NO KEY REQUIRED)
    // Supports TND and other currencies better than Frankfurter
    private static final String BASE_URL = "https://open.er-api.com/v6/latest/%s";

    public static double convert(double amount, String from, String to) {
        if (from.equals(to)) return amount;
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = String.format(BASE_URL, from);
            System.out.println("Currency API Request: " + url);
            
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Currency API Status: " + response.statusCode());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                if (json.has("rates")) {
                    double rate = json.getJSONObject("rates").getDouble(to);
                    return amount * rate;
                }
            } else {
                System.err.println("Currency API Error Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Currency API Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static Map<String, String> getCommonCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        currencies.put("EUR", "Euro");
        currencies.put("USD", "US Dollar");
        currencies.put("GBP", "British Pound");
        currencies.put("TND", "Tunisian Dinar");
        currencies.put("CAD", "Canadian Dollar");
        currencies.put("JPY", "Japanese Yen");
        return currencies;
    }
}
