import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-flat-haze-aotdbt9n-pooler.c-2.ap-southeast-1.aws.neon.tech/krishivani?sslmode=require";
        String user = "krishivani_owner";
        String pass = "u4Rj2SbwYfGq";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT district, arrivals, crop_type FROM market_prices WHERE district='Junagadh' LIMIT 10")) {
             
            System.out.println("Junagadh Prices:");
            while (rs.next()) {
                System.out.println(rs.getString("district") + " - " + rs.getString("crop_type") + " - " + rs.getString("arrivals"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
