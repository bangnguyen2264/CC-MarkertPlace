package com.example.vehicleservice.controller;

import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.entity.Vehicle;
import com.example.vehicleservice.model.filter.VehicleFilter;
import com.example.vehicleservice.model.filter.VehicleTypeFilter;
import com.example.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest vehiclerequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(vehiclerequest));
    }
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll(@Valid @ParameterObject VehicleFilter vehicleFilter) {
        return ResponseEntity.ok(vehicleService.getAll(vehicleFilter));
    }
}
