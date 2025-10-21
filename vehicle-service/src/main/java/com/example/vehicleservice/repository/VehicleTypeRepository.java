package com.example.vehicleservice.repository;

import com.example.vehicleservice.model.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> , JpaSpecificationExecutor<VehicleType> {
}
