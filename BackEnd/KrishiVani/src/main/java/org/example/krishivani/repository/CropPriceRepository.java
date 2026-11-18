package org.example.krishivani.repository;

import org.example.krishivani.model.CropPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropPriceRepository extends JpaRepository<CropPrice, Long> {
    List<CropPrice> findByDistrictAndYard(String district, String yard);
    List<CropPrice> findByDistrict(String district);

    @Query("SELECT cp FROM CropPrice cp WHERE LOWER(cp.district) = LOWER(:district) OR LOWER(cp.district) LIKE LOWER(CONCAT('%', :district, '%'))")
    List<CropPrice> findByDistrictFlexible(@Param("district") String district);

    @Query("SELECT DISTINCT cp.yard FROM CropPrice cp WHERE LOWER(cp.district) LIKE LOWER(CONCAT('%', :district, '%')) ORDER BY cp.yard")
    List<String> findDistinctYardsByDistrict(@Param("district") String district);

    @Query("SELECT DISTINCT cp.yard FROM CropPrice cp ORDER BY cp.yard")
    List<String> findAllDistinctYards();

    @Query("SELECT DISTINCT cp.district FROM CropPrice cp ORDER BY cp.district")
    List<String> findAllDistinctDistricts();
}
