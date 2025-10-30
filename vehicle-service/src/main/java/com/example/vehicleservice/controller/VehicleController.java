package com.example.vehicleservice.controller;

import com.example.commondto.dto.request.UpdateStatusRequest;
import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.filter.VehicleFilter;
import com.example.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<VehicleResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll(@Valid @ParameterObject VehicleFilter vehicleFilter) {
        return ResponseEntity.ok(vehicleService.getAll(vehicleFilter));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật thông tin xe", description = "Cập nhật dữ liệu xe với multipart/form-data")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable("id") String id,
            @ModelAttribute UpdateStatusRequest updateRequest // dùng @ModelAttribute cho multipart
    ) {
        return ResponseEntity.ok(vehicleService.update(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
