package com.example.vehicleservice.service.impl;

import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.BeanCopyUtils;
import com.example.vehicleservice.model.dto.request.VehicleTypeRequest;
import com.example.vehicleservice.model.dto.response.VehicleTypeResponse;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleTypeFilter;
import com.example.vehicleservice.repository.VehicleTypeRepository;
import com.example.vehicleservice.service.VehicleTypeService;
import com.example.commondto.utils.CrudUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    public List<VehicleTypeResponse> addAll(List<VehicleTypeRequest> vehicleTypes) {
        List<VehicleType> vehicleTypeList = vehicleTypes.stream().map(VehicleTypeRequest::to).toList();
        vehicleTypeRepository.saveAll(vehicleTypeList);
        return vehicleTypeList.stream().map(VehicleTypeResponse::from).toList();
    }

    @Override
    public VehicleTypeResponse create(VehicleTypeRequest vehicleType) {
        VehicleType vehicleTypeEntity = vehicleTypeRepository.save(VehicleTypeRequest.to(vehicleType));
        return VehicleTypeResponse.from(vehicleTypeEntity);
    }

    @Override
    public List<VehicleTypeResponse> getAll(VehicleTypeFilter vehicleTypeFilter) {
        Pageable pageable = CrudUtils.createPageable(vehicleTypeFilter);
        Specification<VehicleType> spec = buildFilter(vehicleTypeFilter);
        var result = vehicleTypeRepository.findAll(spec, pageable);
        return result.stream().map(VehicleTypeResponse::from).toList();
    }

    private Specification<VehicleType> buildFilter(VehicleTypeFilter vehicleTypeFilter) {
        Specification<VehicleType> spec = (root, query, cb) -> cb.conjunction();

        if (vehicleTypeFilter.getModel() != null && !vehicleTypeFilter.getModel().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("model"), vehicleTypeFilter.getModel()));
        }

        if (vehicleTypeFilter.getManufacturer() != null && !vehicleTypeFilter.getManufacturer().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("manufacturer"), vehicleTypeFilter.getManufacturer()));
        }

        return spec;
    }


    @Override
    public VehicleTypeResponse getById(String id) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Vehicle Type Not Found"));
        return VehicleTypeResponse.from(vehicleType);
    }

    @Override
    public VehicleTypeResponse update(String id, VehicleTypeRequest vehicleTypeRequest) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Vehicle Type Not Found"));
        try {
            BeanCopyUtils.copyNonNullProperties(vehicleTypeRequest, vehicleType);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }

        return VehicleTypeResponse.from(vehicleTypeRepository.save(vehicleType));
    }

    @Override
    public void delete(String id) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Vehicle Type Not Found"));
        vehicleTypeRepository.delete(vehicleType);
    }


}
