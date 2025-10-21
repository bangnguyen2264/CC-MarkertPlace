package com.example.vehicleservice.service;

import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.entity.Vehicle;
import com.example.vehicleservice.model.filter.VehicleFilter;

import java.util.List;

public interface VehicleService {
    VehicleResponse create(VehicleRequest vehicleRequest);
    List<VehicleResponse> getAll(VehicleFilter vehicleFilter);
    VehicleResponse getById(String id);
    VehicleResponse update(String id, VehicleRequest vehicleRequest);
    void delete(String id);
}
