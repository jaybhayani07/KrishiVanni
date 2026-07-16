package org.example.krishivani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crop_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cropName;
    private String district;
    private String yard;
    private String minPrice;
    private String maxPrice;
    private String modalPrice;
    private String arrivals;
    private String priceTrend;
    private String arrivalTrend;
    private String cropType;
}
