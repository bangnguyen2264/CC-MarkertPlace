package com.example.vehicleservice.service.impl;

import com.example.vehicleservice.exception.NotFoundException;
import com.example.vehicleservice.model.dto.request.VehicleTypeRequest;
import com.example.vehicleservice.model.dto.response.VehicleTypeResponse;
import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.model.filter.VehicleTypeFilter;
import com.example.vehicleservice.repository.VehicleTypeRepository;
import com.example.vehicleservice.service.VehicleTypeService;
import com.example.vehicleservice.utils.CrudUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        var spec = buildFilter(vehicleTypeFilter.getName());
        var result = vehicleTypeRepository.findAll(spec, pageable);
        return result.stream().map(VehicleTypeResponse::from).toList();
    }

 private Specification<VehicleType> buildFilter(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    @Override
    public VehicleTypeResponse getById(Long id) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id).orElseThrow(()-> new NotFoundException("Vehicle Type Not Found"));
        return VehicleTypeResponse.from(vehicleType);
    }

    @Override
    public VehicleTypeResponse update(VehicleTypeRequest vehicleType) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }



}
