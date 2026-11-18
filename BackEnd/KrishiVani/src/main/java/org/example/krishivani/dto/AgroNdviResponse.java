package org.example.krishivani.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgroNdviResponse {
    private Long dt;
    private NdviData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NdviData {
        private Double mean;
        private Double max;
        private Double min;
    }
}
