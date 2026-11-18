package org.example.krishivani.controller;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.model.CropPrice;
import org.example.krishivani.repository.CropPriceRepository;
import org.example.krishivani.service.MarketSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market/prices")
@RequiredArgsConstructor
public class MarketController {

    private final CropPriceRepository cropPriceRepository;
    private final MarketSyncService marketSyncService;

    @GetMapping
    public ResponseEntity<List<CropPrice>> getPrices(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String yard
    ) {
        if (district != null && !district.trim().isEmpty()) {
            String d = district.trim();
            if (yard != null && !yard.trim().isEmpty() && !yard.contains("તમામ યાર્ડ") && !yard.equalsIgnoreCase("all")) {
                return ResponseEntity.ok(cropPriceRepository.findByDistrictAndYard(d, yard.trim()));
            }
            List<CropPrice> list = cropPriceRepository.findByDistrictFlexible(d);
            return ResponseEntity.ok(list);
        } else if (yard != null && !yard.trim().isEmpty() && !yard.contains("તમામ યાર્ડ") && !yard.equalsIgnoreCase("all")) {
            return ResponseEntity.ok(cropPriceRepository.findByDistrictAndYard(district, yard.trim()));
        }
        return ResponseEntity.ok(cropPriceRepository.findAll());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<String>> getDistricts() {
        return ResponseEntity.ok(cropPriceRepository.findAllDistinctDistricts());
    }

    @GetMapping("/yards")
    public ResponseEntity<List<String>> getYards(@RequestParam(required = false) String district) {
        if (district != null && !district.trim().isEmpty()) {
            return ResponseEntity.ok(cropPriceRepository.findDistinctYardsByDistrict(district.trim()));
        }
        return ResponseEntity.ok(cropPriceRepository.findAllDistinctYards());
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncPrices() {
        new Thread(marketSyncService::fetchAndSyncMarketPrices).start();
        return ResponseEntity.ok("Market prices sync started in background.");
    }

    @PostMapping("/mock-data")
    public ResponseEntity<String> loadMockData() {
        if (cropPriceRepository.count() == 0) {
            cropPriceRepository.save(new CropPrice(null, "મગફળી (Groundnut)", "રાજકોટ (Rajkot)", "રાજકોટ", "₹૧,૨૦૦", "₹૧,૪૫૦", "₹૧,૩૫૦", "૫,૪૦૦ ક્વિન્ટલ", "+૨.૫%", "-૧.૨%", "groundnut"));
            cropPriceRepository.save(new CropPrice(null, "કપાસ (Cotton)", "રાજકોટ (Rajkot)", "રાજકોટ", "₹૧,૫૦૦", "₹૧,૮૦૦", "₹૧,૬૫૦", "૩,૨૦૦ ક્વિન્ટલ", "-૧.૦%", "+૦.૫%", "cotton"));
            cropPriceRepository.save(new CropPrice(null, "જીરું (Cumin)", "રાજકોટ (Rajkot)", "ગોંડલ", "₹૪,૦૦૦", "₹૫,૨૦૦", "₹૪,૮૦૦", "૧,૧૦૦ ક્વિન્ટલ", "+૫.૦%", "-૩.૦%", "cumin"));
            return ResponseEntity.ok("Mock data loaded successfully.");
        }
        return ResponseEntity.ok("Mock data already exists.");
    }
}
