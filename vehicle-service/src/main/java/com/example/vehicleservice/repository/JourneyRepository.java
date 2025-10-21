package com.example.vehicleservice.repository;

import com.example.vehicleservice.model.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourneyRepository extends JpaRepository<Journey, String> {
}
