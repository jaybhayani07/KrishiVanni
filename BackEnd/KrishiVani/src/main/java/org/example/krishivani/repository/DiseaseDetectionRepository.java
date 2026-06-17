package org.example.krishivani.repository;

import org.example.krishivani.model.DiseaseDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseDetectionRepository extends JpaRepository<DiseaseDetection, Long> {
}
