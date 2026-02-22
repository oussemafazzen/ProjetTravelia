package utils;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for database connection.
 * This test verifies that the database connection is working properly.
 */
public class MyDataBaseConnectionTest {

    @Test
    void testDatabaseConnection() {
        MyDataBase db = MyDataBase.getInstance();
        assertNotNull(db, "MyDataBase instance should not be null");
        
        Connection cnx = db.getCnx();
        assertNotNull(cnx, "Database connection should not be null");
        
        try {
            assertFalse(cnx.isClosed(), "Database connection should be open");
            assertTrue(cnx.isValid(2), "Database connection should be valid");
            System.out.println("✓ Database connection test passed");
        } catch (SQLException e) {
            fail("Database connection test failed: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseConnectionMetadata() throws SQLException {
        MyDataBase db = MyDataBase.getInstance();
        Connection cnx = db.getCnx();
        
        assertNotNull(cnx.getMetaData(), "Database metadata should be available");
        
        String dbName = cnx.getMetaData().getDatabaseProductName();
        System.out.println("Connected to: " + dbName);
        assertEquals("MySQL", dbName, "Should be connected to MySQL database");
    }

    @Test
    void testSingletonPattern() {
        MyDataBase instance1 = MyDataBase.getInstance();
        MyDataBase instance2 = MyDataBase.getInstance();
        
        assertSame(instance1, instance2, 
            "MyDataBase should follow singleton pattern - both instances should be the same");
    }
}
