package org.example.krishivani.service;

import org.example.krishivani.dto.IrrigationAdviceResponse;
import org.example.krishivani.dto.IrrigationRequest;
import org.example.krishivani.dto.OpenMeteoResponse;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvancedIrrigationService {

    private final OpenMeteoService openMeteoService;

    public IrrigationAdviceResponse getIrrigationAdvice(IrrigationRequest request) {
        // Fetch weather including ET0 (past 7 days and future forecast)
        OpenMeteoResponse weather = openMeteoService.fetchWeather(request.getLat(), request.getLon());
        
        long daysSinceWatering = calculateDaysSinceWatering(request.getLastWateringDate());
        
        // Safety cap on days
        if (daysSinceWatering < 0) daysSinceWatering = 0;
        if (daysSinceWatering > 7) daysSinceWatering = 7; // We only have 7 days history
        
        // Calculate Kc
        double kc = getCropCoefficient(request.getCrop(), request.getStage());
        
        // Calculate Water Deficit over the past X days
        double totalEtc = 0;
        double totalRain = 0;
        
        if (weather != null && weather.getDaily() != null) {
            List<String> dates = weather.getDaily().getTime();
            List<Double> et0List = weather.getDaily().getEt0FaoEvapotranspiration();
            List<Double> rainList = weather.getDaily().getPrecipitationSum();
            
            LocalDate today = LocalDate.now();
            LocalDate lastWateringDate = today.minusDays(daysSinceWatering);
            
            for (int i = 0; i < dates.size(); i++) {
                LocalDate dataDate = LocalDate.parse(dates.get(i));
                
                // If the date is strictly after the last watering date and up to today
                if (dataDate.isAfter(lastWateringDate) && (dataDate.isBefore(today) || dataDate.isEqual(today))) {
                    
                    if (et0List != null && et0List.size() > i && et0List.get(i) != null) {
                        totalEtc += et0List.get(i) * kc;
                    }
                    if (rainList != null && rainList.size() > i && rainList.get(i) != null) {
                        totalRain += rainList.get(i);
                    }
                }
            }
        }
        
        double deficit = totalEtc - totalRain;
        if (deficit < 0) deficit = 0; // Rain exceeded ETc
        
        IrrigationAdviceResponse response = new IrrigationAdviceResponse();
        response.setDeficitMm(deficit);
        
        // Simple logic for advice based on soil type and crop
        double threshold = getSoilThreshold(request.getSoilType(), request.getCrop(), request.getStage());
        
        if (deficit >= threshold) {
            response.setAdviceGu(String.format("જમીનમાં %.1f mm પાણીની અછત છે. પાક સુકાઈ રહ્યો છે, આજે જ પિયત આપો.", deficit));
            response.setAdviceEn(String.format("Soil water deficit is %.1f mm. Crop is under stress, irrigate today.", deficit));
            response.setDaysToNextIrrigation(0);
        } else {
            // Predict future ETc based on Gujarat research data
            double avgDailyEtc = getAverageDailyEtc(request.getCrop(), request.getStage());
            int daysRemaining = (int) Math.ceil((threshold - deficit) / avgDailyEtc);
            if (daysRemaining < 0) daysRemaining = 0;
            
            if (daysRemaining == 0) {
                response.setAdviceGu("જમીનમાં ભેજ ઓછો થઈ રહ્યો છે. આજે અથવા કાલે પિયત આપવાની તૈયારી રાખો.");
                response.setAdviceEn("Moisture is reducing. Prepare to irrigate today or tomorrow.");
            } else if (daysRemaining <= 2) {
                response.setAdviceGu(String.format("જમીનમાં ભેજ ઘટી રહ્યો છે. %d દિવસમાં પિયત આપવાની તૈયારી રાખો.", daysRemaining));
                response.setAdviceEn(String.format("Moisture is decreasing. Prepare to irrigate in %d days.", daysRemaining));
            } else {
                response.setAdviceGu(String.format("જમીનમાં ભેજનું પ્રમાણ શ્રેષ્ઠ છે. %d દિવસ પછી પિયત આપો.", daysRemaining));
                response.setAdviceEn(String.format("Optimal soil moisture. Irrigate after %d days.", daysRemaining));
            }
            
            response.setDaysToNextIrrigation(daysRemaining);
        }
        
        return response;
    }

    private double getCropCoefficient(String crop, String stage) {
        if (crop == null) crop = "";
        if (stage == null) stage = "";
        crop = crop.toLowerCase();
        stage = stage.toLowerCase();
        
        // "Sowing" (Initial), "Growth" (Dev/Mid), "Flowering" (Mid/Late)
        boolean isGroundnut = crop.contains("groundnut") || crop.contains("મગફળી");
        boolean isCotton = crop.contains("cotton") || crop.contains("કપાસ");
        boolean isWheat = crop.contains("wheat") || crop.contains("ઘઉં");
        boolean isOnion = crop.contains("onion") || crop.contains("ડુંગળી");
        boolean isSoybean = crop.contains("soyabean") || crop.contains("સોયાબીન");
        boolean isCastor = crop.contains("castor") || crop.contains("દિવેલા") || crop.contains("એરંડા");
        boolean isSpices = crop.contains("cumin") || crop.contains("જીરું") || crop.contains("coriander") || crop.contains("ધાણા");
        boolean isSesame = crop.contains("sesame") || crop.contains("તલ");
        
        // Defaults
        double kcInitial = 0.4;
        double kcMid = 1.15;
        double kcLate = 0.7;

        if (isGroundnut) {
            kcInitial = 0.40; kcMid = 1.10; kcLate = 0.55;
        } else if (isCotton || isCastor) {
            kcInitial = 0.35; kcMid = 1.15; kcLate = 0.50; // Castor behaves similarly to Cotton
        } else if (isWheat) {
            kcInitial = 0.40; kcMid = 1.15; kcLate = 0.41;
        } else if (isOnion) {
            kcInitial = 0.50; kcMid = 1.05; kcLate = 0.75;
        } else if (isSoybean) {
            kcInitial = 0.40; kcMid = 1.05; kcLate = 0.40; // From Table 1
        } else if (isSpices) {
            kcInitial = 0.35; kcMid = 1.15; kcLate = 0.35; // Proxied via Mustard from Table 1
        } else if (isSesame) {
            kcInitial = 0.40; kcMid = 0.96; kcLate = 0.51; // Proxied via Green gram from Table 1
        }

        if (stage.contains("sowing") || stage.contains("વાવણી")) return kcInitial;
        if (stage.contains("growth") || stage.contains("વૃદ્ધિ")) return (kcInitial + kcMid) / 2.0;
        if (stage.contains("flowering") || stage.contains("ફૂલ")) return kcMid;
        
        return kcMid; // Default
    }

    private double getAverageDailyEtc(String crop, String stage) {
        if (crop == null) crop = "";
        if (stage == null) stage = "";
        crop = crop.toLowerCase();
        stage = stage.toLowerCase();
        
        boolean isInit = stage.contains("sowing") || stage.contains("વાવણી");
        boolean isMid = stage.contains("flowering") || stage.contains("ફૂલ");
        boolean isDev = stage.contains("growth") || stage.contains("વૃદ્ધિ");

        if (crop.contains("groundnut") || crop.contains("મગફળી")) {
            if (isInit) return 2.7;
            if (isDev) return 4.7;
            if (isMid) return 7.8;
            return 6.4;
        } else if (crop.contains("cotton") || crop.contains("કપાસ") || crop.contains("castor") || crop.contains("દિવેલા")) {
            if (isInit) return 2.4;
            if (isDev) return 3.9;
            if (isMid) return 6.5;
            return 3.6;
        } else if (crop.contains("wheat") || crop.contains("ઘઉં")) {
            if (isInit) return 1.8;
            if (isDev) return 3.2;
            if (isMid) return 5.6;
            return 4.7;
        } else if (crop.contains("soyabean") || crop.contains("સોયાબીન")) {
            if (isInit) return 3.0;
            if (isDev) return 3.6;
            if (isMid) return 5.6;
            return 4.2;
        } else if (crop.contains("cumin") || crop.contains("જીરું") || crop.contains("coriander") || crop.contains("ધાણા")) {
            if (isInit) return 1.6;
            if (isDev) return 3.2;
            if (isMid) return 5.1;
            return 4.1;
        } else if (crop.contains("sesame") || crop.contains("તલ")) {
            if (isInit) return 2.1;
            if (isDev) return 3.3;
            if (isMid) return 5.4;
            return 4.3;
        } else if (crop.contains("onion") || crop.contains("ડુંગળી")) {
            if (isInit) return 2.5;
            if (isDev) return 4.0;
            if (isMid) return 6.0;
            return 4.5;
        }
        
        // Default using Kc
        double kc = getCropCoefficient(crop, stage);
        return kc * 5.0; // Assume 5mm/day average ET0
    }

    private double getSoilThreshold(String soilType, String crop, String stage) {
        if (soilType == null) return 25.0;
        soilType = soilType.toLowerCase();
        if (crop == null) crop = "";
        crop = crop.toLowerCase();
        if (stage == null) stage = "";
        stage = stage.toLowerCase();

        // Adjust for crop rooting depth and water demand
        double cropMultiplier = 1.0;
        if (crop.contains("groundnut") || crop.contains("મગફળી")) {
            cropMultiplier = 0.6; // Groundnut needs frequent watering
        } else if (crop.contains("onion") || crop.contains("ડુંગળી")) {
            cropMultiplier = 0.5; // Onion has very shallow roots
        } else if (crop.contains("cumin") || crop.contains("જીરું") || crop.contains("coriander") || crop.contains("ધાણા")) {
            cropMultiplier = 0.7; // Spices have shallow roots
        } else if (crop.contains("cotton") || crop.contains("કપાસ") || crop.contains("castor") || crop.contains("દિવેલા")) {
            cropMultiplier = 1.2; // Cotton/Castor are deep-rooted and drought tolerant
        } else if (crop.contains("wheat") || crop.contains("ઘઉં")) {
            cropMultiplier = 0.9;
        } else if (crop.contains("soyabean") || crop.contains("સોયાબીન") || crop.contains("sesame") || crop.contains("તલ")) {
            cropMultiplier = 0.8;
        }

        // Base Threshold in mm of water deficit before irrigation is needed.
        double baseThreshold = 30.0;
        if (soilType.contains("sandy") || soilType.contains("ગોરાડુ")) baseThreshold = 18.0;
        else if (soilType.contains("black") || soilType.contains("કાળી")) baseThreshold = 35.0;
        else if (soilType.contains("alluvial") || soilType.contains("કાંપવાળી")) baseThreshold = 28.0;
        else if (soilType.contains("saline") || soilType.contains("ખારી")) baseThreshold = 20.0;
        
        double finalThreshold = baseThreshold * cropMultiplier;
        
        // At Sowing stage, roots are very shallow, so the top soil dries out fast. Reduce threshold by 50%
        if (stage.contains("sowing") || stage.contains("વાવણી")) {
            finalThreshold *= 0.5;
        }
        
        return finalThreshold;
    }

    private long calculateDaysSinceWatering(String lastWateringDate) {
        if (lastWateringDate == null || lastWateringDate.trim().isEmpty()) {
            return 0; // Assume watered today if empty
        }
        
        try {
            // Format is like "25/5/2026" or "25/05/2026"
            if (lastWateringDate.contains("/")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                LocalDate parsedDate = LocalDate.parse(lastWateringDate, formatter);
                return ChronoUnit.DAYS.between(parsedDate, LocalDate.now());
            }
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + lastWateringDate);
        }
        return 0;
    }
}
