package org.example.krishivani.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.krishivani.model.CropPrice;
import org.example.krishivani.repository.CropPriceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketSyncService {

    private final CropPriceRepository cropPriceRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${datagov.api.key:}")
    private String apiKey;

    @Value("${datagov.resource.id:}")
    private String resourceId;


    @PostConstruct
    public void init() {
        new Thread(() -> {
            if (cropPriceRepository.count() == 0) {
                log.info("Database is empty. Running initial Data.gov.in sync in background...");
                fetchAndSyncMarketPrices();
                return;
            }
            log.info("Market Yard Data already exists in database. Skipping startup sync. Background cron job will handle future updates.");
        }, "MarketSyncThread").start();
    }


    @Scheduled(cron = "0 0 6,14,20 * * ?")
    public void fetchAndSyncMarketPrices() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_DATA_GOV_API_KEY") ||
            resourceId == null || resourceId.isEmpty() || resourceId.equals("YOUR_RESOURCE_ID")) {
            log.warn("Data.gov.in API Key or Resource ID is not configured. Skipping market sync.");
            return;
        }

        try {
            log.info("Starting Data.gov.in Market Yard Sync for Gujarat...");

            java.time.LocalDate date = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            JsonNode records = null;
            for (int i = 0; i < 7; i++) {
                String dateStr = date.minusDays(i).format(dtf);
                log.info("Trying to fetch market prices for date: " + dateStr);

                java.net.URI uri = UriComponentsBuilder.fromHttpUrl("https://api.data.gov.in/resource/" + resourceId)
                        .queryParam("api-key", apiKey)
                        .queryParam("format", "json")
                        .queryParam("limit", 10000)
                        .queryParam("filters[State]", "Gujarat")
                        .queryParam("filters[Arrival_Date]", dateStr)
                        .build()
                        .encode()
                        .toUri();

                String response = restTemplate.getForObject(uri, String.class);
                if (response != null) {
                    JsonNode root = objectMapper.readTree(response);
                    records = root.get("records");
                    if (records != null && records.isArray() && records.size() > 0) {
                        log.info("Found " + records.size() + " records for date: " + dateStr);
                        break;
                    }
                }
            }

            if (records != null && records.isArray() && records.size() > 0) {
                List<CropPrice> newPrices = new ArrayList<>();
                    java.util.Map<String, CropPrice> uniquePrices = new java.util.HashMap<>();
                    java.util.Map<String, java.util.Date> latestDates = new java.util.HashMap<>();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

                    for (JsonNode record : records) {
                        String state = record.has("State") ? record.get("State").asText() : "";


                        if (state.equalsIgnoreCase("Gujarat")) {
                            String cropName = record.has("Commodity") ? record.get("Commodity").asText() : "Unknown";
                            String variety = record.has("Variety") ? record.get("Variety").asText() : "";
                            if (!variety.isEmpty() && !variety.equalsIgnoreCase(cropName) && !variety.equalsIgnoreCase("Other")) {
                                cropName = cropName + " (" + variety + ")";
                            }
                            String district = record.has("District") ? record.get("District").asText() : "Unknown";
                            String yard = record.has("Market") ? record.get("Market").asText() : "Unknown";


                            String cnLower = cropName.toLowerCase();
                            if (cnLower.contains("groundnut") || cnLower.contains("ground nut")) cropName = "મગફળી (Groundnut)";
                            else if (cnLower.contains("cotton")) cropName = "કપાસ (Cotton)";
                            else if (cnLower.contains("cumin") || cnLower.contains("cummin") || cnLower.contains("jeera")) cropName = "જીરું (Cumin)";
                            else if (cnLower.contains("wheat")) cropName = "ઘઉં (Wheat)";
                            else if (cnLower.contains("onion")) cropName = "ડુંગળી (Onion)";
                            else if (cnLower.contains("red gram") || cnLower.contains("tur") || cnLower.contains("arhar") || cnLower.contains("pigeon pea") || cnLower.contains("pegeon pea")) cropName = "તુવેર (Tur)";
                            else if (cnLower.contains("sesamum") || cnLower.contains("sesame") || cnLower.contains("til")) cropName = "તલ (Sesame)";
                            else if (cnLower.contains("castor")) cropName = "એરંડા (Castor)";
                            else if (cnLower.contains("mustard") || cnLower.contains("rai")) cropName = "રાયડો (Mustard)";
                            else if (cnLower.contains("bengal gram") || cnLower.contains("chana") || cnLower.contains("chickpeas")) cropName = "ચણા (Gram)";
                            else if (cnLower.contains("soyabean") || cnLower.contains("soya")) cropName = "સોયાબીન (Soyabean)";

                            String key = district + "_" + yard + "_" + cropName;

                            java.util.Date arrivalDate = null;
                            try {
                                String dateStr = record.has("Arrival_Date") ? record.get("Arrival_Date").asText() : "";
                                if (!dateStr.isEmpty()) {
                                    arrivalDate = sdf.parse(dateStr);
                                }
                            } catch (Exception e) {}

                            if (uniquePrices.containsKey(key)) {
                                java.util.Date existingDate = latestDates.get(key);
                                if (arrivalDate == null) continue;
                                if (existingDate != null && !arrivalDate.after(existingDate)) {
                                    continue;
                                }
                            }
                            latestDates.put(key, arrivalDate);

                            CropPrice cp = new CropPrice();
                            cp.setCropName(cropName);
                            cp.setDistrict(district);
                            cp.setYard(yard);
                            String minPriceStr = record.has("Min_Price") ? record.get("Min_Price").asText() : "";
                            String maxPriceStr = record.has("Max_Price") ? record.get("Max_Price").asText() : "";
                            String modalPriceStr = record.has("Modal_Price") ? record.get("Modal_Price").asText() : "";


                            cp.setMinPrice("₹" + convertTo20Kg(minPriceStr));
                            cp.setMaxPrice("₹" + convertTo20Kg(maxPriceStr));
                            cp.setModalPrice("₹" + convertTo20Kg(modalPriceStr));


                            cp.setArrivals(record.has("Arrival_Date") ? record.get("Arrival_Date").asText() : "-");
                            cp.setPriceTrend("-");
                            cp.setArrivalTrend("-");
                            cp.setCropType(cropName.toLowerCase());

                            uniquePrices.put(key, cp);
                        }
                    }

                    newPrices = new ArrayList<>(uniquePrices.values());
                    if (!newPrices.isEmpty()) {

                        cropPriceRepository.deleteAllInBatch();
                        cropPriceRepository.saveAll(newPrices);
                        log.info("Successfully synced {} market records from Data.gov.in", newPrices.size());


                        try {
                            Files.writeString(Paths.get("last_sync.txt"), Instant.now().toString());
                        } catch (IOException ignored) {}

                    } else {
                        log.warn("No records found for Gujarat in Data.gov.in response.");
                    }
            }
        } catch (Exception e) {
            log.error("Failed to sync market prices from Data.gov.in: {}", e.getMessage());
        }
    }


    private String convertTo20Kg(String price100kg) {
        if (price100kg == null || price100kg.isEmpty() || price100kg.equals("-") || price100kg.equals("0")) {
            return "-";
        }
        try {
            double pricePerQuintal = Double.parseDouble(price100kg);
            double pricePerMaund = pricePerQuintal / 5.0;
            return String.valueOf(Math.round(pricePerMaund));
        } catch (NumberFormatException e) {
            return price100kg;
        }
    }
}
