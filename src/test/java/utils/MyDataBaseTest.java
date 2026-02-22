package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection configuration for testing purposes.
 * This class provides a separate database connection for unit tests
 * to avoid interfering with production data.
 * 
 * Note: Currently configured to use the same database as production.
 * For better test isolation, consider using an H2 in-memory database
 * or a separate test database instance.
 */
public class MyDataBaseTest {
    private static MyDataBaseTest instance;
    
    // Test database configuration
    // TODO: Consider using H2 in-memory database for better test isolation:
    // final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    final String URL = "jdbc:mysql://127.0.0.1:3306/voyage";
    final String USERNAME = "root";
    final String PASSWORD = "";
    
    private Connection cnx;

    private MyDataBaseTest() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Test Database Connected....");
        } catch (SQLException e) {
            System.err.println("Test Database Connection Failed: " + e.getMessage());
        }
    }

    public static MyDataBaseTest getInstance() {
        if (instance == null)
            instance = new MyDataBaseTest();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
    
    /**
     * Close the database connection (useful for cleanup after tests)
     */
    public void closeConnection() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("Test Database Connection Closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing test database connection: " + e.getMessage());
        }
    }
}
