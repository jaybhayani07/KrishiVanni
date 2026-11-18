package org.example.krishivani.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.krishivani.dto.OpenMeteoResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OpenMeteoService {

    private final RestTemplate restTemplate;

    public OpenMeteoService() {
        this.restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    public OpenMeteoResponse fetchWeather(double latitude, double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("current", "temperature_2m,relative_humidity_2m,weather_code,soil_temperature_0cm,soil_moisture_0_to_1cm")
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max,et0_fao_evapotranspiration,precipitation_sum")
                .queryParam("timezone", "Asia/Kolkata")
                .queryParam("past_days", 7)
                .toUriString();

        return restTemplate.getForObject(url, OpenMeteoResponse.class);
    }
    
    public String getWeatherConditionGu(int weatherCode) {
        if (weatherCode == 0) return "સન્ની / સાફ";
        if (weatherCode <= 3) return "મોટેભાગે સન્ની";
        if (weatherCode <= 48) return "ધુમ્મસવાળું";
        if (weatherCode <= 67) return "વરસાદી";
        if (weatherCode <= 77) return "બરફવર્ષા";
        if (weatherCode <= 99) return "વાવાઝોડું";
        return "અજ્ઞાત";
    }

    public String getWeatherConditionEn(int weatherCode) {
        if (weatherCode == 0) return "Clear sky";
        if (weatherCode <= 3) return "Mostly Sunny";
        if (weatherCode <= 48) return "Foggy";
        if (weatherCode <= 67) return "Rain";
        if (weatherCode <= 77) return "Snow";
        if (weatherCode <= 99) return "Thunderstorm";
        return "Unknown";
    }
}
