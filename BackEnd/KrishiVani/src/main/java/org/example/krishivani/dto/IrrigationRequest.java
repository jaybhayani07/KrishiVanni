package org.example.krishivani.dto;

import lombok.Data;

@Data
public class IrrigationRequest {
    private String crop;
    private String stage;
    private String soilType;
    private String lastWateringDate; // e.g. "25/5/2026"
    private double lat = 21.5222;
    private double lon = 70.4579;
}
