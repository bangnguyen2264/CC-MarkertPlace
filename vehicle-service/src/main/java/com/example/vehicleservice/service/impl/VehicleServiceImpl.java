package com.example.vehicleservice.service.impl;

import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.filter.VehicleFilter;
import com.example.vehicleservice.repository.VehicleRepository;
import com.example.vehicleservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleResponse create(VehicleRequest vehicleRequest) {
        return null;
    }

    @Override
    public List<VehicleResponse> getAll(VehicleFilter vehicleFilter) {
        return List.of();
    }

    @Override
    public VehicleResponse getById(Long id) {
        return null;
    }

    @Override
    public VehicleResponse update(Long id, VehicleRequest vehicleRequest) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

}
