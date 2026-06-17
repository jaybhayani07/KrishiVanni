package org.example.krishivani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "home_dashboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeDashboardData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Weather
    private String location;
    private String temperature;
    private String conditionGu;
    private String conditionEn;
    private String highLow;
    private String soilMoisture;
    private String soilTemperature;
    
    // Satellite Crop Health (NDVI)
    private String cropHealthStatusGu;
    private String cropHealthStatusEn;
    private String ndviValue;
    private String lastScanDate;
    private String nextScanDate;
    
    @Column(length = 1000)
    private String satelliteImageUrl;
    
    // Crop Health Warning
    private String warningTitleGu;
    
    // Forecast data (Not stored in DB, transient for frontend)
    @Transient
    private java.util.List<DailyForecast> forecast;

    @Data
    public static class DailyForecast {
        private String dayNameGu;
        private String maxTemp;
        private String iconName; // e.g., "Sunny", "Cloud", "Rain"
    }
    private String warningTitleEn;
    private String warningDescGu;
    
    // Irrigation Advice
    private String irrigationRecommendationGu;
    private String irrigationDays;
    
    // YouTube Weather System (Transient)
    @Transient
    private java.util.List<String> expertFarmingTips;
    
    @Transient
    private String youtubeLastUpdated;
    
    @Transient
    private String pareshForecast;
    
    @Transient
    private String ambalalForecast;

    @Transient
    private String agreementPercentage;

    @Transient
    private Boolean pareshIsLive;

    @Transient
    private Boolean ambalalIsLive;

    @Transient
    private Boolean youtubeIsRunning;

    @Transient
    private Integer youtubeProgress;

    @Transient
    private String youtubeProgressMessage;
}
