import utils.MyDataBase;
import java.sql.*;

public class RunSQL {
    public static void main(String[] args) throws Exception {
        Connection cnx = MyDataBase.getInstance().getCnx();
        
        System.out.println("--- Table: Reservation ---");
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("DESCRIBE reservation")) {
            while(rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type") + " | " + rs.getString("Null"));
            }
        }
        
        System.out.println("\n--- Table: Billet ---");
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("DESCRIBE billet")) {
            while(rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type") + " | " + rs.getString("Null"));
            }
        }
    }
}
