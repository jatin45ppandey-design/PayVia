
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDb {

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "010505");
            Statement stmt = conn.createStatement();

            // Check if DB exists
            try {
                stmt.executeUpdate("CREATE DATABASE payvia_test");
                System.out.println("Database created successfully");
            } catch (Exception e) {
                System.out.println("Database might already exist: " + e.getMessage());
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
