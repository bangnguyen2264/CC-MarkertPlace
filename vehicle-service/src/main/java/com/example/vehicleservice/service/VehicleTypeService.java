package com.example.vehicleservice.service;

import com.example.vehicleservice.model.dto.request.VehicleTypeRequest;
import com.example.vehicleservice.model.dto.response.VehicleTypeResponse;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleTypeFilter;

import java.util.List;

public interface VehicleTypeService {
    List<VehicleTypeResponse> addAll(List<VehicleTypeRequest> vehicleTypes);
    VehicleTypeResponse create(VehicleTypeRequest vehicleType);
    List<VehicleTypeResponse> getAll(VehicleTypeFilter vehicleTypeFilter);
    VehicleTypeResponse getById(String id);
    VehicleTypeResponse update(String id,VehicleTypeRequest vehicleType);
    void delete(String id);
}
