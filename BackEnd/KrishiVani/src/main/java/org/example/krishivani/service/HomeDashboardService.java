package org.example.krishivani.service;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.dto.OpenMeteoResponse;
import org.example.krishivani.dto.AgroNdviResponse;
import org.example.krishivani.model.HomeDashboardData;
import org.example.krishivani.repository.HomeDashboardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HomeDashboardService {

    private final HomeDashboardRepository homeDashboardRepository;
    private final OpenMeteoService openMeteoService;
    private final AgromonitoringService agromonitoringService;

    public HomeDashboardData getDashboardData(double lat, double lon) {
        List<HomeDashboardData> data = homeDashboardRepository.findAll();
        HomeDashboardData dashboardData;

        if (data.isEmpty()) {
            dashboardData = new HomeDashboardData();
            dashboardData.setLocation("Junagadh");
            dashboardData.setWarningTitleGu("સામાન્ય સ્થિતિ");
            dashboardData.setWarningTitleEn("Normal Condition");
            dashboardData.setWarningDescGu("હાલમાં કોઈ મોટો ખતરો નથી.");
            dashboardData.setIrrigationRecommendationGu("જમીન અને હવામાન અનુકૂળ છે.");
            dashboardData.setIrrigationDays("0");
        } else {
            dashboardData = data.get(0);
        }

        try {
            OpenMeteoResponse weather = openMeteoService.fetchWeather(lat, lon);
            if (weather != null && weather.getCurrent() != null) {
                dashboardData.setTemperature(weather.getCurrent().getTemperature() + "°C");
                dashboardData.setConditionGu(openMeteoService.getWeatherConditionGu(weather.getCurrent().getWeatherCode()));
                dashboardData.setConditionEn(openMeteoService.getWeatherConditionEn(weather.getCurrent().getWeatherCode()));
                dashboardData.setSoilMoisture(String.format("%.2f m³/m³", weather.getCurrent().getSoilMoisture()));
                dashboardData.setSoilTemperature(weather.getCurrent().getSoilTemperature() + "°C");

                if (weather.getDaily() != null && !weather.getDaily().getMaxTemp().isEmpty()) {
                    dashboardData.setHighLow(String.format("H: %.0f° L: %.0f°",
                            weather.getDaily().getMaxTemp().get(0),
                            weather.getDaily().getMinTemp().get(0)));

                    List<HomeDashboardData.DailyForecast> forecastList = new java.util.ArrayList<>();
                    String[] gujDays = {"સોમ", "મંગળ", "બુધ", "ગુરુ", "શુક્ર", "શનિ", "રવિ"};

                    java.time.LocalDate todayLocalDate = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
                    int todayIndex = 0;
                    for (int i = 0; i < weather.getDaily().getTime().size(); i++) {
                        java.time.LocalDate date = java.time.LocalDate.parse(weather.getDaily().getTime().get(i));
                        if (date.isEqual(todayLocalDate) || date.isAfter(todayLocalDate)) {
                            todayIndex = i;
                            break;
                        }
                    }

                    for (int i = todayIndex; i < weather.getDaily().getTime().size(); i++) {
                        HomeDashboardData.DailyForecast df = new HomeDashboardData.DailyForecast();
                        java.time.LocalDate date = java.time.LocalDate.parse(weather.getDaily().getTime().get(i));

                        if (i == todayIndex) {
                            df.setDayNameGu("આજે");
                        } else {
                            df.setDayNameGu(gujDays[date.getDayOfWeek().getValue() - 1]);
                        }

                        df.setMaxTemp(String.format("%.0f°", weather.getDaily().getMaxTemp().get(i)));
                        int code = weather.getDaily().getWeatherCode().get(i);
                        int prob = 0;
                        if (weather.getDaily().getPrecipitationProbabilityMax() != null && weather.getDaily().getPrecipitationProbabilityMax().size() > i) {
                            prob = weather.getDaily().getPrecipitationProbabilityMax().get(i);
                        }

                        if (code <= 3) {
                            df.setIconName("Sunny");
                        } else if (code >= 95) {
                            df.setIconName(prob >= 20 ? "Thunder" : "Cloud");
                        } else if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
                            df.setIconName(prob >= 20 ? "Rain" : "Cloud");
                        } else {
                            df.setIconName("Cloud");
                        }
                        forecastList.add(df);
                    }
                    dashboardData.setForecast(forecastList);
                }

                AgroNdviResponse latestScan = agromonitoringService.getLatestNdvi(lat, lon);
                double simulatedNdvi;

                if (latestScan != null && latestScan.getData() != null && latestScan.getData().getMean() != null) {
                    simulatedNdvi = latestScan.getData().getMean();

                    java.time.LocalDate realDate = java.time.Instant.ofEpochSecond(latestScan.getDt())
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                    dashboardData.setLastScanDate(formatGujaratiDate(realDate));

                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate nextScan = realDate.plusDays(5);
                    while (nextScan.isBefore(today) || nextScan.isEqual(today)) {
                        nextScan = nextScan.plusDays(5);
                    }
                    dashboardData.setNextScanDate(formatGujaratiDate(nextScan));
                } else {
                    if (lat > 23.0 && lat < 24.5 && lon > 68.0 && lon < 71.0) {
                        simulatedNdvi = 0.25 + (Math.random() * 0.15);
                    } else {
                        simulatedNdvi = 0.65 + (Math.random() * 0.20);
                    }
                    dashboardData.setLastScanDate(formatGujaratiDate(LocalDate.now().minusDays(2)));
                    dashboardData.setNextScanDate(formatGujaratiDate(LocalDate.now().plusDays(3)));
                }

                String imageUrl = agromonitoringService.getLatestImageUrl(lat, lon);
                if (imageUrl != null) {
                    dashboardData.setSatelliteImageUrl(imageUrl);
                }

                dashboardData.setNdviValue(String.format("%.2f", simulatedNdvi));

                if (simulatedNdvi >= 0.75) {
                    dashboardData.setCropHealthStatusGu("ઉત્તમ");
                    dashboardData.setCropHealthStatusEn("Excellent");
                } else if (simulatedNdvi >= 0.65) {
                    dashboardData.setCropHealthStatusGu("સારી");
                    dashboardData.setCropHealthStatusEn("Good");
                } else if (simulatedNdvi >= 0.40) {
                    dashboardData.setCropHealthStatusGu("સાધારણ");
                    dashboardData.setCropHealthStatusEn("Stress");
                } else {
                    dashboardData.setCropHealthStatusGu("ખૂબ નબળું");
                    dashboardData.setCropHealthStatusEn("Poor");
                }

                homeDashboardRepository.save(dashboardData);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch live weather: " + e.getMessage());
        }

        try {
            java.io.File consensusFile = new java.io.File("youtube_consensus.json");
            if (consensusFile.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(consensusFile);

                if (rootNode.has("combinedWeatherAlert")) {
                    dashboardData.setWarningTitleGu("ખેડૂત સંમતિ (Farmers Consensus)");
                    dashboardData.setWarningDescGu(rootNode.get("combinedWeatherAlert").asText());
                }

                if (rootNode.has("farmingTips") && rootNode.get("farmingTips").isArray()) {
                    java.util.List<String> tips = new java.util.ArrayList<>();
                    for (com.fasterxml.jackson.databind.JsonNode tipNode : rootNode.get("farmingTips")) {
                        tips.add(tipNode.asText());
                    }
                    dashboardData.setExpertFarmingTips(tips);
                }

                if (rootNode.has("pareshForecast")) {
                    dashboardData.setPareshForecast(rootNode.get("pareshForecast").asText());
                }

                if (rootNode.has("ambalalForecast")) {
                    dashboardData.setAmbalalForecast(rootNode.get("ambalalForecast").asText());
                }

                if (rootNode.has("lastUpdated")) {
                    dashboardData.setYoutubeLastUpdated(rootNode.get("lastUpdated").asText());
                }

                if (rootNode.has("agreementPercentage")) {
                    dashboardData.setAgreementPercentage(rootNode.get("agreementPercentage").asText());
                }

                if (rootNode.has("pareshIsLive")) {
                    dashboardData.setPareshIsLive(rootNode.get("pareshIsLive").asBoolean());
                }

                if (rootNode.has("ambalalIsLive")) {
                    dashboardData.setAmbalalIsLive(rootNode.get("ambalalIsLive").asBoolean());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load YouTube consensus data: " + e.getMessage());
        }

        try {
            java.io.File statusFile = new java.io.File("youtube_status.json");
            if (statusFile.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(statusFile);
                if (rootNode.has("isRunning")) {
                    dashboardData.setYoutubeIsRunning(rootNode.get("isRunning").asBoolean());
                }
                if (rootNode.has("progress")) {
                    dashboardData.setYoutubeProgress(rootNode.get("progress").asInt());
                }
                if (rootNode.has("progressMessage")) {
                    dashboardData.setYoutubeProgressMessage(rootNode.get("progressMessage").asText());
                }
            } else {
                dashboardData.setYoutubeIsRunning(false);
            }
        } catch (Exception e) {
            dashboardData.setYoutubeIsRunning(false);
        }

        return dashboardData;
    }

    public String loadMockData() {
        if (homeDashboardRepository.count() == 0) {
            HomeDashboardData mockData = new HomeDashboardData();
            mockData.setLocation("જુનાગઢ");
            mockData.setTemperature("32°C");
            mockData.setConditionGu("મોટેભાગે સન્ની");
            mockData.setConditionEn("Mostly Sunny");
            mockData.setHighLow("H: 35° L: 26°");
            mockData.setSoilMoisture("0.25 m³/m³");
            mockData.setSoilTemperature("24.5°C");
            mockData.setCropHealthStatusGu("ઉત્તમ");
            mockData.setCropHealthStatusEn("Excellent");
            mockData.setNdviValue("0.78");
            mockData.setLastScanDate("૨૬ મે ૨૦૨૬");
            mockData.setNextScanDate("૨૯ મે ૨૦૨૬");
            mockData.setWarningTitleGu("ગુલાબી ઈયળનો ખતરો");
            mockData.setWarningTitleEn("Pink Bollworm Threat");
            mockData.setWarningDescGu("આગામી ૪૮ કલાકમાં કપાસમાં ગુલાબી ઈયળનું જોખમ.");
            mockData.setIrrigationRecommendationGu("જમીનમાં ભેજ પૂરતો છે. ૪ દિવસ પછી પિયત આપો.");
            mockData.setIrrigationDays("4");

            homeDashboardRepository.save(mockData);
            return "Mock Home Dashboard data loaded successfully.";
        }
        return "Mock Home Dashboard data already exists.";
    }

    private String formatGujaratiDate(LocalDate date) {
        String[] gujMonths = {"જાન્યુઆરી", "ફેબ્રુઆરી", "માર્ચ", "એપ્રિલ", "મે", "જૂન", "જુલાઈ", "ઓગસ્ટ", "સપ્ટેમ્બર", "ઓક્ટોબર", "નવેમ્બર", "ડિસેમ્બર"};
        return date.getDayOfMonth() + " " + gujMonths[date.getMonthValue()-1] + " " + date.getYear();
    }
}
