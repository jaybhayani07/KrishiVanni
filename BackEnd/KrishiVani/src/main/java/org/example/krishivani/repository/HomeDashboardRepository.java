package org.example.krishivani.repository;

import org.example.krishivani.model.HomeDashboardData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeDashboardRepository extends JpaRepository<HomeDashboardData, Long> {
}
