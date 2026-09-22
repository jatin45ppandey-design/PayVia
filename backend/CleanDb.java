import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CleanDb {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/payvia_test", "postgres", "010505");
            Statement stmt = conn.createStatement();
            stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
            System.out.println("Cleaned!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
