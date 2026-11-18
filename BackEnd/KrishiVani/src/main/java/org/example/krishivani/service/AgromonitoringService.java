package org.example.krishivani.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.krishivani.dto.AgroNdviResponse;
import org.example.krishivani.dto.AgroImageResponse;
import org.example.krishivani.dto.AgroPolygonRequest;
import org.example.krishivani.dto.AgroPolygonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgromonitoringService {

    @Value("${agromonitoring.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://api.agromonitoring.com/agro/1.0";
    private final ConcurrentHashMap<String, String> polyCache = new ConcurrentHashMap<>();

    public AgromonitoringService() {
        this.restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    public AgroNdviResponse getLatestNdvi(double lat, double lon) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        try {
            String polyId = getOrFindPolygonId(lat, lon);
            if (polyId == null) {
                return null;
            }

            long endTimestamp = (System.currentTimeMillis() / 1000) - 86400;
            long startTimestamp = endTimestamp - (60L * 24 * 60 * 60);

            String ndviUrl = BASE_URL + "/ndvi/history?polyid=" + polyId +
                    "&start=" + startTimestamp + "&end=" + endTimestamp + "&appid=" + apiKey;

            AgroNdviResponse[] responses = restTemplate.getForObject(ndviUrl, AgroNdviResponse[].class);

            if (responses != null && responses.length > 0) {
                AgroNdviResponse latestScan = responses[0];
                for (AgroNdviResponse r : responses) {
                    if (r.getDt() != null && latestScan.getDt() != null && r.getDt() > latestScan.getDt()) {
                        latestScan = r;
                    }
                }
                if (latestScan.getData() != null && latestScan.getData().getMean() != null) {
                    return latestScan;
                }
            }
        } catch (Exception e) {
            System.err.println("Agromonitoring NDVI Error: " + e.getMessage());
        }
        return null;
    }

    public String getLatestImageUrl(double lat, double lon) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        try {
            String polyId = getOrFindPolygonId(lat, lon);
            if (polyId == null) {
                return null;
            }

            long endTimestamp = (System.currentTimeMillis() / 1000) - 86400;
            long startTimestamp = endTimestamp - (60L * 24 * 60 * 60);

            String imageUrl = BASE_URL + "/image/search?polyid=" + polyId +
                    "&start=" + startTimestamp + "&end=" + endTimestamp + "&appid=" + apiKey;

            AgroImageResponse[] responses = restTemplate.getForObject(imageUrl, AgroImageResponse[].class);

            if (responses != null && responses.length > 0) {
                AgroImageResponse latestImage = responses[0];
                for (AgroImageResponse r : responses) {
                    if (r.getDt() != null && latestImage.getDt() != null && r.getDt() > latestImage.getDt()) {
                        latestImage = r;
                    }
                }
                if (latestImage.getImage() != null && latestImage.getImage().getNdvi() != null) {
                    String url = latestImage.getImage().getNdvi();
                    if (url.startsWith("http://")) {
                        url = url.replace("http://", "https://");
                    }
                    return url;
                }
            }
        } catch (Exception e) {
            System.err.println("Agromonitoring Image API Error: " + e.getMessage());
        }
        return null;
    }

    private String getOrFindPolygonId(double lat, double lon) {
        String cacheKey = String.format("%.2f_%.2f", lat, lon);
        String polyId = polyCache.get(cacheKey);
        if (polyId != null) {
            return polyId;
        }

        try {
            String listUrl = BASE_URL + "/polygons?appid=" + apiKey;
            AgroPolygonResponse[] existing = restTemplate.getForObject(listUrl, AgroPolygonResponse[].class);
            if (existing != null) {
                for (AgroPolygonResponse p : existing) {
                    if (p.getCenter() != null && p.getCenter().length >= 2) {
                        double pLon = p.getCenter()[0];
                        double pLat = p.getCenter()[1];
                        if (Math.abs(pLat - lat) < 0.05 && Math.abs(pLon - lon) < 0.05) {
                            polyCache.put(cacheKey, p.getId());
                            return p.getId();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        polyId = createPolygon(lat, lon);
        if (polyId != null) {
            polyCache.put(cacheKey, polyId);
        }
        return polyId;
    }

    private String createPolygon(double lat, double lon) {
        String url = BASE_URL + "/polygons?appid=" + apiKey;

        AgroPolygonRequest request = new AgroPolygonRequest();
        request.setName("Auto_Field_" + System.currentTimeMillis());

        AgroPolygonRequest.GeoJson geoJson = new AgroPolygonRequest.GeoJson();
        AgroPolygonRequest.Geometry geometry = new AgroPolygonRequest.Geometry();

        double offset = 0.002;
        geometry.setCoordinates(new double[][][]{
                {
                        {lon - offset, lat + offset},
                        {lon + offset, lat + offset},
                        {lon + offset, lat - offset},
                        {lon - offset, lat - offset},
                        {lon - offset, lat + offset}
                }
        });

        geoJson.setGeometry(geometry);
        request.setGeo_json(geoJson);

        try {
            AgroPolygonResponse response = restTemplate.postForObject(url, request, AgroPolygonResponse.class);
            if (response != null && response.getId() != null) {
                return response.getId();
            }
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            if (errorBody != null && errorBody.contains("already existed polygon")) {
                Pattern pattern = Pattern.compile("polygon '([a-f0-9]+)'");
                Matcher matcher = pattern.matcher(errorBody);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create polygon: " + e.getMessage());
        }
        return null;
    }
}
