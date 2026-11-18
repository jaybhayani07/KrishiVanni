package org.example.krishivani.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {
    private CurrentWeather current;
    private DailyWeather daily;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        @JsonProperty("temperature_2m")
        private Double temperature;

        @JsonProperty("relative_humidity_2m")
        private Integer humidity;

        @JsonProperty("weather_code")
        private Integer weatherCode;

        @JsonProperty("soil_temperature_0cm")
        private Double soilTemperature;

        @JsonProperty("soil_moisture_0_to_1cm")
        private Double soilMoisture;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyWeather {
        @JsonProperty("temperature_2m_max")
        private List<Double> maxTemp;

        @JsonProperty("temperature_2m_min")
        private List<Double> minTemp;

        @JsonProperty("time")
        private List<String> time;

        @JsonProperty("weather_code")
        private List<Integer> weatherCode;

        @JsonProperty("precipitation_probability_max")
        private List<Integer> precipitationProbabilityMax;

        @JsonProperty("et0_fao_evapotranspiration")
        private List<Double> et0FaoEvapotranspiration;

        @JsonProperty("precipitation_sum")
        private List<Double> precipitationSum;
    }
}
