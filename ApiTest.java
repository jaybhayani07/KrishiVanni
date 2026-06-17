import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public class ApiTest {
    public static void main(String[] args) throws Exception {
        String url = "https://api.data.gov.in/resource/35985678-0d79-46b4-9ed6-6f13308a1d24?api-key=579b464db66ec23bdd000001802a49aa749b40884843c7e5f57e8f8d&format=json&limit=10000&filters[State]=Gujarat&filters[Arrival_Date]=02/06/2026";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode records = root.get("records");
        
        int junagadhCount = 0;
        if (records != null && records.isArray()) {
            System.out.println("Total records for 02/06: " + records.size());
            for (JsonNode record : records) {
                if (record.has("District") && record.get("District").asText().equals("Junagadh")) {
                    junagadhCount++;
                }
            }
        }
        System.out.println("Junagadh records for 02/06: " + junagadhCount);
    }
}
