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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

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
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}

            boolean shouldSync = false;
            long count = cropPriceRepository.count();
            if (count == 0) {
                log.info("Database is empty. Running initial Data.gov.in sync in background...");
                shouldSync = true;
            } else {
                File syncFile = new File("last_sync.txt");
                if (!syncFile.exists()) {
                    shouldSync = true;
                } else {
                    try {
                        String lastSyncIso = Files.readString(syncFile.toPath()).trim();
                        Instant lastSyncTime = Instant.parse(lastSyncIso);
                        long hoursSince = java.time.Duration.between(lastSyncTime, Instant.now()).toHours();
                        if (hoursSince >= 6) {
                            log.info("Last Data.gov.in sync was {} hours ago. Triggering fresh sync...", hoursSince);
                            shouldSync = true;
                        } else {
                            log.info("Market Yard Data is fresh (synced {} hours ago). Skipping startup sync.", hoursSince);
                        }
                    } catch (Exception e) {
                        shouldSync = true;
                    }
                }
            }

            if (shouldSync) {
                fetchAndSyncMarketPrices();
            }
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

            LocalDate date = LocalDate.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            List<JsonNode> allDayRecords = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String dateStr = date.minusDays(i).format(dtf);
                log.info("Fetching market prices for date: {}", dateStr);

                try {
                    java.net.URI uri = UriComponentsBuilder.fromHttpUrl("https://api.data.gov.in/resource/" + resourceId)
                            .queryParam("api-key", apiKey)
                            .queryParam("format", "json")
                            .queryParam("limit", 1000)
                            .queryParam("filters[State]", "Gujarat")
                            .queryParam("filters[Arrival_Date]", dateStr)
                            .build()
                            .encode()
                            .toUri();

                    String response = restTemplate.getForObject(uri, String.class);
                    if (response != null) {
                        JsonNode root = objectMapper.readTree(response);
                        JsonNode records = root.get("records");
                        if (records != null && records.isArray() && records.size() > 0) {
                            log.info("Found {} records for date: {}", records.size(), dateStr);
                            for (JsonNode rec : records) {
                                allDayRecords.add(rec);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Failed fetching records for date {}: {}", dateStr, ex.getMessage());
                }

                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {}
            }

            if (!allDayRecords.isEmpty()) {
                Map<String, CropPrice> uniquePrices = new HashMap<>();
                Map<String, Date> latestDates = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                for (JsonNode record : allDayRecords) {
                    String state = record.has("State") ? record.get("State").asText() : "";

                    if (state.equalsIgnoreCase("Gujarat")) {
                        String cropName = record.has("Commodity") ? record.get("Commodity").asText() : "Unknown";
                        String variety = record.has("Variety") ? record.get("Variety").asText() : "";
                        if (!variety.isEmpty() && !variety.equalsIgnoreCase(cropName) && !variety.equalsIgnoreCase("Other")) {
                            cropName = cropName + " (" + variety + ")";
                        }
                        String rawDistrict = record.has("District") ? record.get("District").asText().trim() : "Unknown";
                        String yard = record.has("Market") ? record.get("Market").asText().trim() : "Unknown";

                        String district = normalizeDistrict(rawDistrict, yard);

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

                        Date arrivalDate = null;
                        try {
                            String dateStr = record.has("Arrival_Date") ? record.get("Arrival_Date").asText() : "";
                            if (!dateStr.isEmpty()) {
                                arrivalDate = sdf.parse(dateStr);
                            }
                        } catch (Exception ignored) {}

                        if (uniquePrices.containsKey(key)) {
                            Date existingDate = latestDates.get(key);
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

                List<CropPrice> newPrices = new ArrayList<>(uniquePrices.values());
                if (!newPrices.isEmpty()) {
                    cropPriceRepository.deleteAllInBatch();
                    cropPriceRepository.saveAll(newPrices);
                    log.info("Successfully synced {} market records from Data.gov.in across {} districts", newPrices.size(),
                            cropPriceRepository.findAllDistinctDistricts().size());

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

    private String normalizeDistrict(String rawDistrict, String yard) {
        String yl = yard.toLowerCase();
        String dl = rawDistrict.toLowerCase();

        if (yl.contains("modasa") || yl.contains("bayad") || yl.contains("bhiloda") ||
            yl.contains("dhansura") || yl.contains("meghraj") || yl.contains("tintoi") || yl.contains("malpur")) {
            return "Aravalli";
        }
        if (yl.contains("vyara") || yl.contains("vyra") || yl.contains("songadh") ||
            yl.contains("nizar") || yl.contains("valod") || yl.contains("uchchhal")) {
            return "Tapi";
        }
        if (yl.contains("lunawada") || yl.contains("luna") || yl.contains("balasinor") ||
            yl.contains("santrampur") || yl.contains("kadana") || yl.contains("virpur")) {
            return "Mahisagar";
        }

        if (dl.contains("dang")) return "Dang";
        if (dl.contains("junagarh") || dl.contains("junagadh")) return "Junagadh";
        if (dl.contains("banaskan")) return "Banaskantha";
        if (dl.contains("kachchh") || dl.contains("kutch")) return "Kutch";
        if (dl.contains("vadodara") || dl.contains("baroda")) return "Vadodara";
        if (dl.contains("panchmahal")) return "Panchmahal";
        if (dl.contains("sabarkan")) return "Sabarkantha";
        if (dl.contains("surendranagar")) return "Surendranagar";
        if (dl.contains("bhavnagar")) return "Bhavnagar";
        if (dl.contains("jamnagar")) return "Jamnagar";
        if (dl.contains("porbandar")) return "Porbandar";
        if (dl.contains("rajkot")) return "Rajkot";
        if (dl.contains("amreli")) return "Amreli";
        if (dl.contains("morbi")) return "Morbi";
        if (dl.contains("botad")) return "Botad";
        if (dl.contains("gir somnath")) return "Gir Somnath";
        if (dl.contains("dwarka")) return "Devbhumi Dwarka";
        if (dl.contains("gandhinagar")) return "Gandhinagar";
        if (dl.contains("mehsana")) return "Mehsana";
        if (dl.contains("patan")) return "Patan";
        if (dl.contains("ahmedabad")) return "Ahmedabad";
        if (dl.contains("kheda")) return "Kheda";
        if (dl.contains("anand")) return "Anand";
        if (dl.contains("dahod")) return "Dahod";
        if (dl.contains("chhota")) return "Chhota Udaipur";
        if (dl.contains("surat")) return "Surat";
        if (dl.contains("navsari")) return "Navsari";
        if (dl.contains("valsad")) return "Valsad";
        if (dl.contains("bharuch")) return "Bharuch";
        if (dl.contains("narmada")) return "Narmada";

        return rawDistrict;
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
