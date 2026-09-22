import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestPasswords {
    public static void main(String[] args) {
        String[] passwords = {"postgres", "password", "admin", "root", "123456", "1234", "secret", "test", "qwerty", ""};
        for (String p : passwords) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", p);
                System.out.println("SUCCESS: '" + p + "'");
                conn.close();
                return;
            } catch (SQLException e) {
                // ignore
            }
        }
        System.out.println("FAILED ALL");
    }
}
