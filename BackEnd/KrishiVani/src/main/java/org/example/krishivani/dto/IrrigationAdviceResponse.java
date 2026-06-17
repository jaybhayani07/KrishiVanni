package org.example.krishivani.dto;

import lombok.Data;

@Data
public class IrrigationAdviceResponse {
    private String adviceGu;
    private String adviceEn;
    private int daysToNextIrrigation;
    private double deficitMm;
}
