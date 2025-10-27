package com.example.vehicleservice.service.impl;

import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.BeanCopyUtils;
import com.example.vehicleservice.integration.UserValidationIntegration;
import com.example.vehicleservice.model.dto.request.VehicleRequest;
import com.example.vehicleservice.model.dto.response.VehicleResponse;
import com.example.vehicleservice.model.entity.Journey;
import com.example.vehicleservice.model.entity.Vehicle;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleFilter;
import com.example.vehicleservice.repository.JourneyRepository;
import com.example.vehicleservice.repository.VehicleRepository;
import com.example.vehicleservice.repository.VehicleTypeRepository;
import com.example.vehicleservice.service.VehicleService;
import com.example.commondto.utils.CrudUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserValidationIntegration userValidationIntegration;
    private final JourneyRepository journeyRepository;

    public VehicleResponse create(VehicleRequest vehicleRequest) {
        log.info("Creating vehicle with request: {}", vehicleRequest);

        UserValidationResponse response = userValidationIntegration.validateUser(vehicleRequest.getOwnerId(), null).join();

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
        Journey journey = Journey.builder()
                .vehicle(vehicle)
                .distanceKm(0.0)
                .averageSpeed(0.0)
                .energyUsed(0.0)
                .co2Reduced(0.0)
                .build();
        vehicle.setJourney(journey);
        log.info("Saving vehicle: {}", vehicle);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        journeyRepository.save(journey);
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
    public VehicleResponse getById(String id) {
        return vehicleRepository.findById(id).map(VehicleResponse::from).orElseThrow(
                () -> new NotFoundException("Vehicle not found with id " + id)
        );

    }

    @Override
    public VehicleResponse update(String id, VehicleRequest vehicleRequest) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Vehicle not found with id " + id)
        );
        try {
            BeanCopyUtils.copyNonNullProperties(vehicleRequest, vehicle);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to update vehicle", e);
        }
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    public void delete(String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Vehicle not found with id " + id)
        );
        vehicleRepository.delete(vehicle);
    }

}
