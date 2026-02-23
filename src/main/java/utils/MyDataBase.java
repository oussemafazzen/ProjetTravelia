package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String URL = "jdbc:mysql://localhost:3306/GestionUtilisateur";
    private final String USER = "root";
    private final String PASSWORD = "";
    private Connection connection;
    private static MyDataBase instance;

    private MyDataBase() {
        try {
            System.out.println("Tentative de connexion à la base de données...");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec succès !");
        } catch (SQLException e) {
            System.err.println("ERREUR CONNEXION BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
