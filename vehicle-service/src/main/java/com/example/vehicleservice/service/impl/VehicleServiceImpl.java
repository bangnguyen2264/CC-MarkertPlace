package com.example.vehicleservice.service.impl;

import com.example.commondto.dto.response.UserValidationResponse;
import com.example.vehicleservice.exception.NotFoundException;
import com.example.vehicleservice.integration.UserValidationGateway;
import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.entity.Vehicle;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleFilter;
import com.example.vehicleservice.repository.VehicleRepository;
import com.example.vehicleservice.repository.VehicleTypeRepository;
import com.example.vehicleservice.service.VehicleService;
import com.example.vehicleservice.utils.CrudUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserValidationGateway validationGateway;

    public VehicleResponse create(VehicleRequest vehicleRequest) {
        log.info("Creating vehicle with request: {}", vehicleRequest);

        UserValidationResponse response = validationGateway.validateUser(vehicleRequest.getOwnerId(), null).join();

        if (response == null || !response.isValid()) {
            throw new NotFoundException("User validation failed: " +
                    (response != null ? response.getMessage() : "No response from user-service"));
        }

        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleRequest.getVehicleTypeId())
                .orElseThrow(() -> {
                    log.error("Vehicle type not found with id: {}", vehicleRequest.getVehicleTypeId());
                    return new NotFoundException("Vehicle type not found with id " + vehicleRequest.getVehicleTypeId());
                });

        Vehicle vehicle = Vehicle.builder()
                .ownerId(vehicleRequest.getOwnerId())
                .vehicleType(vehicleType)
                .vin(vehicleRequest.getVin())
                .licensePlate(vehicleRequest.getLicensePlate())
                .mileage(vehicleRequest.getMileage())
                .registrationDate(vehicleRequest.getRegistrationDate())
                .registrationImageUrl(vehicleRequest.getRegistrationImageUrl())
                .registrationNumber(vehicleRequest.getRegistrationNumber())
                .color(vehicleRequest.getColor())
                .note(vehicleRequest.getNote())
                .build();

        log.info("Saving vehicle: {}", vehicle);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle saved successfully with id: {}", savedVehicle.getId());

        return VehicleResponse.from(savedVehicle);
    }

    @Override
    public List<VehicleResponse> getAll(VehicleFilter vehicleFilter) {
        Pageable pageable = CrudUtils.createPageable(vehicleFilter);
        Specification<Vehicle> spec = buildFilter(vehicleFilter);
        Page<Vehicle> result = vehicleRepository.findAll(spec, pageable);
        return result.stream().map(VehicleResponse::from).toList();
    }

    // 🔹 Hàm buildFilter hoàn chỉnh cho VehicleFilter
    private Specification<Vehicle> buildFilter(VehicleFilter vehicleFilter) {
        Specification<Vehicle> spec = (root, query, cb) -> cb.conjunction();

        if (vehicleFilter.getOwnerId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("ownerId"), vehicleFilter.getOwnerId()));
        }

        if (vehicleFilter.getVin() != null && !vehicleFilter.getVin().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("vin")), "%" + vehicleFilter.getVin().toLowerCase() + "%"));
        }

        if (vehicleFilter.getVehicleTypeId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("vehicleType").get("id"), vehicleFilter.getVehicleTypeId()));
        }

        // Nếu có trường enabled (hoặc verified), có thể lọc theo đó
        if (vehicleFilter.isEnabled()) {
            spec = spec.and((root, query, cb) ->
                    cb.isTrue(root.get("verified")));
        }

        return spec;
    }


    @Override
    public VehicleResponse getById(Long id) {
        return vehicleRepository.findById(id).map(VehicleResponse::from).orElseThrow(
                () -> new NotFoundException("Vehicle not found with id " + id)
        );
    }

    @Override
    public VehicleResponse update(Long id, VehicleRequest vehicleRequest) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

}
