package com.example.vehicleservice.controller;

import com.example.vehicleservice.model.dto.request.VehicleTypeRequest;
import com.example.vehicleservice.model.dto.response.VehicleTypeResponse;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleTypeFilter;
import com.example.vehicleservice.security.RoleRequired;
import com.example.vehicleservice.service.VehicleTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicle-types")
public class VehicleTypeController {
    private final VehicleTypeService vehicleTypeService;
    @RoleRequired("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<VehicleTypeResponse> create(@Valid @RequestBody VehicleTypeRequest vehicleType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleTypeService.create(vehicleType));
    }
    @RoleRequired("ROLE_ADMIN")
    @PostMapping("/add-all")
    public ResponseEntity<List<VehicleTypeResponse>> addAll(@Valid @RequestBody List<VehicleTypeRequest> vehicleTypes) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleTypeService.addAll(vehicleTypes));
    }
    @GetMapping
    public ResponseEntity<List<VehicleTypeResponse>> getAllVehicleTypes(@Valid @ParameterObject VehicleTypeFilter vehicleTypeFilter) {
        return ResponseEntity.ok(vehicleTypeService.getAll(vehicleTypeFilter));
    }
    @GetMapping("/{id}")
    public ResponseEntity<VehicleTypeResponse> getVehicleTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleTypeService.getById(id));
    }
}
