package services;

import models.Client;
import models.enums.Role;
import models.enums.Statut;
import utils.MyDataBase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import java.util.Properties;

import org.json.JSONObject;

public class GoogleAuthService {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authUrl;
    private String tokenUrl;
    private String userInfoUrl;
    private Connection cnx;

    public GoogleAuthService() {
        cnx = MyDataBase.getInstance().getConnection();
        loadConfig();
    }

    private void loadConfig() {
        try {
            Properties props = new Properties();
            InputStream is = getClass().getResourceAsStream("/config.properties");
            if (is != null) {
                props.load(is);
                clientId = props.getProperty("google.client_id", "");
                clientSecret = props.getProperty("google.client_secret", "");
                redirectUri = props.getProperty("google.redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
                authUrl = props.getProperty("google.auth_url", "https://accounts.google.com/o/oauth2/v2/auth");
                tokenUrl = props.getProperty("google.token_url", "https://oauth2.googleapis.com/token");
                userInfoUrl = props.getProperty("google.userinfo_url", "https://www.googleapis.com/oauth2/v3/userinfo");
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement config Google: " + e.getMessage());
        }
    }

    /**
     * Construit l'URL de redirection vers Google pour authentification.
     */
    public String getAuthorizationUrl() {
        try {
            return authUrl + "?" +
                    "client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode("openid email profile", "UTF-8") +
                    "&access_type=offline";
        } catch (Exception e) {
            System.err.println("Erreur construction URL Google Auth: " + e.getMessage());
            return "";
        }
    }

    /**
     * Échange le code d'autorisation contre un access token.
     */
    public String exchangeCodeForToken(String authCode) {
        try {
            String params = "code=" + URLEncoder.encode(authCode, "UTF-8") +
                    "&client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                    "&grant_type=authorization_code";

            HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes("UTF-8"));
            }

            if (conn.getResponseCode() >= 400) {
                InputStream es = conn.getErrorStream();
                if (es != null) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(es));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    System.err.println("Google OAuth Error Response: " + errorResponse.toString());
                }
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            String accessToken = json.optString("access_token");
            System.out.println("Google Access Token obtenu.");
            return accessToken;
        } catch (Exception e) {
            System.err.println("Erreur échange code Google: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Récupère les informations de l'utilisateur Google (email, nom).
     * @return JSONObject avec les champs: sub (google_id), email, name, given_name, family_name
     */
    public JSONObject getUserInfo(String accessToken) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(userInfoUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject userInfo = new JSONObject(response.toString());
            System.out.println("Google User Info: " + userInfo.toString());
            return userInfo;
        } catch (Exception e) {
            System.err.println("Erreur récupération info Google: " + e.getMessage());
            return null;
        }
    }

    /**
     * Recherche un utilisateur par son Google ID.
     */
    public Client findByGoogleId(String googleId) throws SQLException {
        String req = "SELECT * FROM client WHERE google_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, googleId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToClient(rs);
        }
        return null;
    }

    /**
     * Crée ou récupère un utilisateur via Google.
     * Si l'email existe déjà : lie le compte Google au compte existant.
     * Sinon : crée un nouveau compte.
     */
    public Client findOrCreateUser(String googleId, String email, String name) throws SQLException {
        // Check if user with this Google ID already exists
        Client existing = findByGoogleId(googleId);
        if (existing != null) {
            return existing;
        }

        // Check if email already exists (link Google to existing account)
        String checkReq = "SELECT * FROM client WHERE email = ?";
        PreparedStatement psCheck = cnx.prepareStatement(checkReq);
        psCheck.setString(1, email);
        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) {
            // Link Google ID to existing account
            String updateReq = "UPDATE client SET google_id = ?, email_confirmed = TRUE WHERE email = ?";
            PreparedStatement psUpdate = cnx.prepareStatement(updateReq);
            psUpdate.setString(1, googleId);
            psUpdate.setString(2, email);
            psUpdate.executeUpdate();
            System.out.println("Compte Google lié au compte existant: " + email);
            return mapResultSetToClient(rs);
        }

        // Create new account
        String[] nameParts = name.split(" ", 2);
        String prenom = nameParts.length > 0 ? nameParts[0] : "";
        String nom = nameParts.length > 1 ? nameParts[1] : "";

        String insertReq = "INSERT INTO client (nom, prenom, email, password, telephone, nationalite, date_naissance, role, statut, date_creation, points_fidelite, niveau_fidelite, google_id, email_confirmed) " +
                "VALUES (?, ?, ?, ?, '', '', NULL, 'USER', 'ACTIF', NOW(), 0, 'BRONZE', ?, TRUE)";
        PreparedStatement psInsert = cnx.prepareStatement(insertReq, Statement.RETURN_GENERATED_KEYS);
        psInsert.setString(1, nom);
        psInsert.setString(2, prenom);
        psInsert.setString(3, email);
        psInsert.setString(4, ""); // No password for Google-only accounts
        psInsert.setString(5, googleId);
        psInsert.executeUpdate();

        ResultSet generatedKeys = psInsert.getGeneratedKeys();
        if (generatedKeys.next()) {
            int newId = generatedKeys.getInt(1);
            System.out.println("Nouveau compte Google créé: " + email + " (id=" + newId + ")");

            Client newClient = new Client();
            newClient.setId(newId);
            newClient.setNom(nom);
            newClient.setPrenom(prenom);
            newClient.setEmail(email);
            newClient.setRole(Role.USER);
            newClient.setStatut(Statut.ACTIF);
            newClient.setGoogleId(googleId);
            newClient.setEmailConfirmed(true);
            return newClient;
        }
        return null;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setEmail(rs.getString("email"));
        c.setTelephone(rs.getString("telephone"));
        c.setNationalite(rs.getString("nationalite"));
        c.setDate_naissance(rs.getDate("date_naissance"));

        String roleStr = rs.getString("role");
        try {
            c.setRole(Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            c.setRole(Role.USER);
        }

        c.setStatut(Statut.valueOf(rs.getString("statut")));
        c.setDate_creation(rs.getTimestamp("date_creation"));
        c.setDerniere_connexion(rs.getTimestamp("derniere_connexion"));
        c.setPoints_fidelite(rs.getInt("points_fidelite"));

        String niveauStr = rs.getString("niveau_fidelite");
        try {
            c.setNiveau_fidelite(models.enums.NiveauFidelite.valueOf(niveauStr));
        } catch (Exception e) {
            c.setNiveau_fidelite(models.enums.NiveauFidelite.BRONZE);
        }

        c.setGoogleId(rs.getString("google_id"));
        c.setEmailConfirmed(rs.getBoolean("email_confirmed"));
        return c;
    }

    public String getClientId() {
        return clientId;
    }
}
