package org.example.krishivani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disease_detections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String diseaseNameGu;
    private String diseaseNameEn;
    private String confidence;
    private String treatmentRecommendationGu;
    private String treatmentRecommendationEn;
}
