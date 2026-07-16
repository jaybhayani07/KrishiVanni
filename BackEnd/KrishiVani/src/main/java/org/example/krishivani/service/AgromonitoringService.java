package org.example.krishivani.service;

import org.example.krishivani.dto.AgroNdviResponse;
import org.example.krishivani.dto.AgroImageResponse;
import org.example.krishivani.dto.AgroPolygonRequest;
import org.example.krishivani.dto.AgroPolygonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgromonitoringService {

    @Value("${agromonitoring.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://api.agromonitoring.com/agro/1.0";


    private final ConcurrentHashMap<String, String> polyCache = new ConcurrentHashMap<>();

    public AgroNdviResponse getLatestNdvi(double lat, double lon) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        try {

            String cacheKey = String.format("%.2f_%.2f", lat, lon);
            String polyId = polyCache.get(cacheKey);

            if (polyId == null) {
                System.out.println("🌍 Requesting new Polygon from Agromonitoring...");
                polyId = createPolygon(lat, lon);
                if (polyId != null) {
                    System.out.println("✅ Polygon created successfully! PolyID: " + polyId);
                    polyCache.put(cacheKey, polyId);
                } else {
                    System.out.println("❌ Failed to create Polygon. Falling back to simulation.");
                    return null;
                }
            } else {
                System.out.println("♻️ Using cached Polygon: " + polyId);
            }


            long endTimestamp = (System.currentTimeMillis() / 1000) - 86400;
            long startTimestamp = endTimestamp - (30 * 24 * 60 * 60);

            String ndviUrl = BASE_URL + "/ndvi/history?polyid=" + polyId +
                    "&start=" + startTimestamp + "&end=" + endTimestamp + "&appid=" + apiKey;

            AgroNdviResponse[] responses = restTemplate.getForObject(ndviUrl, AgroNdviResponse[].class);

            if (responses != null && responses.length > 0) {

                AgroNdviResponse latestScan = responses[0];
                for (AgroNdviResponse r : responses) {
                    if (r.getDt() != null && r.getDt() > latestScan.getDt()) {
                        latestScan = r;
                    }
                }
                if (latestScan.getData() != null && latestScan.getData().getMean() != null) {
                    Double realNdvi = latestScan.getData().getMean();
                    System.out.println("✅ REAL Satellite NDVI fetched: " + realNdvi + " on " + latestScan.getDt());
                    return latestScan;
                }
            } else {
                System.out.println("⚠️ Agromonitoring returned empty data. It might take time to process the new polygon.");
            }
        } catch (Exception e) {
            System.err.println("Agromonitoring API Error: " + e.getMessage());
        }
        return null;
    }

    public String getLatestImageUrl(double lat, double lon) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        try {
            String cacheKey = String.format("%.2f_%.2f", lat, lon);
            String polyId = polyCache.get(cacheKey);

            if (polyId == null) {

                return null;
            }

            long endTimestamp = (System.currentTimeMillis() / 1000) - 86400;
            long startTimestamp = endTimestamp - (30 * 24 * 60 * 60);

            String imageUrl = BASE_URL + "/image/search?polyid=" + polyId +
                    "&start=" + startTimestamp + "&end=" + endTimestamp + "&appid=" + apiKey;

            AgroImageResponse[] responses = restTemplate.getForObject(imageUrl, AgroImageResponse[].class);

            if (responses != null && responses.length > 0) {

                AgroImageResponse latestImage = responses[0];
                for (AgroImageResponse r : responses) {
                    if (r.getDt() != null && r.getDt() > latestImage.getDt()) {
                        latestImage = r;
                    }
                }
                if (latestImage.getImage() != null && latestImage.getImage().getNdvi() != null) {
                    System.out.println("📸 REAL Satellite Image URL fetched: " + latestImage.getImage().getNdvi());
                    return latestImage.getImage().getNdvi();
                }
            }
        } catch (Exception e) {
            System.err.println("Agromonitoring Image API Error: " + e.getMessage());
        }
        return null;
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
            if (response != null) {
                return response.getId();
            }
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("❌ API Error: " + e.getStatusCode() + " - " + errorBody);



            if (e.getStatusCode().value() == 422 && errorBody.contains("already existed polygon")) {
                try {
                    int startIndex = errorBody.indexOf("polygon '") + 9;
                    int endIndex = errorBody.indexOf("'", startIndex);
                    if (startIndex > 8 && endIndex > startIndex) {
                        String existingPolyId = errorBody.substring(startIndex, endIndex);
                        System.out.println("♻️ Recovered existing Polygon ID from Agromonitoring: " + existingPolyId);
                        return existingPolyId;
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to parse existing poly ID.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create polygon: " + e.getMessage());
        }
        return null;
    }
}
