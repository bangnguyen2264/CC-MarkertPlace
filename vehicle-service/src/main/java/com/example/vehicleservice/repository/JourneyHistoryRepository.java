package com.example.vehicleservice.repository;

import com.example.vehicleservice.model.entity.JourneyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourneyHistoryRepository extends JpaRepository<JourneyHistory, Long> {
}
