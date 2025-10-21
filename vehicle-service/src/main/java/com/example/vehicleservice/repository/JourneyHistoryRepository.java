package com.example.vehicleservice.repository;

import com.example.vehicleservice.model.entity.JourneyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JourneyHistoryRepository extends JpaRepository<JourneyHistory, String>, JpaSpecificationExecutor<JourneyHistory> {
}
