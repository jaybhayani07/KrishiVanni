package org.example.krishivani.dto;

import lombok.Data;

@Data
public class AgroNdviResponse {
    private Long dt;
    private NdviData data;

    @Data
    public static class NdviData {
        private Double mean;
        private Double max;
        private Double min;
    }
}
