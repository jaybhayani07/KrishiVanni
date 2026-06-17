package org.example.krishivani.repository;

import org.example.krishivani.model.CropPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropPriceRepository extends JpaRepository<CropPrice, Long> {
    List<CropPrice> findByDistrictAndYard(String district, String yard);
    List<CropPrice> findByDistrict(String district);
}
