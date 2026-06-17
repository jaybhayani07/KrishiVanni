import org.springframework.web.util.UriComponentsBuilder;

public class TestUrl {
    public static void main(String[] args) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.data.gov.in/resource/35985678-0d79-46b4-9ed6-6f13308a1d24")
                .queryParam("api-key", "test")
                .queryParam("format", "json")
                .queryParam("limit", 2000)
                .queryParam("filters[State]", "Gujarat")
                .queryParam("sort[Arrival_Date]", "desc")
                .build()
                .encode()
                .toUriString();
        System.out.println(url);
    }
}
