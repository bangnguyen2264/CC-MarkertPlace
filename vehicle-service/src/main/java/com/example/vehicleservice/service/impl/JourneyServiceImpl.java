package com.example.vehicleservice.service.impl;

import com.example.commondto.constant.Status;
import com.example.commondto.dto.request.UpdateStatusRequest;
import com.example.commondto.exception.ConflictException;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.BeanCopyUtils;
import com.example.commondto.utils.CrudUtils;
import com.example.vehicleservice.integration.VerifyCreationIntegration;
import com.example.vehicleservice.model.dto.request.JourneyHistoryRequest;
import com.example.vehicleservice.model.dto.response.JourneyHistoryResponse;
import com.example.vehicleservice.model.dto.response.JourneyResponse;
import com.example.vehicleservice.model.entity.Journey;
import com.example.vehicleservice.model.entity.JourneyHistory;
import com.example.vehicleservice.model.filter.JourneyFilter;
import com.example.vehicleservice.repository.JourneyHistoryRepository;
import com.example.vehicleservice.repository.JourneyRepository;
import com.example.vehicleservice.service.JourneyService;
import com.example.vehicleservice.utils.ConvertHelper;
import com.example.vehicleservice.utils.JourneyUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class JourneyServiceImpl implements JourneyService {
    private final JourneyRepository journeyRepository;
    private final JourneyHistoryRepository journeyHistoryRepository;
    private final VerifyCreationIntegration verifyCreationIntegration;
    private final ConvertHelper convertHelper;

    @Override
    public List<JourneyHistoryResponse> getAllJourneyHistory(JourneyFilter journeyFilter) {
        Pageable pageable = CrudUtils.createPageable(journeyFilter);
        Specification<JourneyHistory> spec = _buildFilter(journeyFilter);
        Page<JourneyHistory> result = journeyHistoryRepository.findAll(spec, pageable);
        return result.stream().map(
                JourneyHistoryResponse::from
        ).collect(Collectors.toList());
    }

    private Specification<JourneyHistory> _buildFilter(JourneyFilter journeyFilter) {
        Specification<JourneyHistory> spec = (root, query, cb) -> cb.conjunction();

        if (journeyFilter.getJourneyId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("journey").get("id"), journeyFilter.getJourneyId()));
        }
        if (journeyFilter.getJourneyStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), journeyFilter.getJourneyStatus()));
        }

        return spec;
    }

    @Override
    public JourneyResponse getJourneyById(String id) {
        Journey journey = journeyRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Journey with id " + id + " not found")
        );
        return JourneyResponse.from(journey);
    }

    @Override
    public JourneyHistoryResponse getJourneyHistoryByJourneyId(String id) {
        JourneyHistory journeyHistory = journeyHistoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Journey with id " + id + " not found")
        );
        return JourneyHistoryResponse.from(journeyHistory);
    }

    @Override
    public JourneyHistoryResponse createJourneyHistory( JourneyHistoryRequest journeyHistoryRequest) {
        Journey journey = journeyRepository.findById(journeyHistoryRequest.getJourneyId()).orElseThrow(
                () -> new NotFoundException("Journey with id " + journeyHistoryRequest.getJourneyId() + " not found")
        );
        JourneyHistory journeyHistory = JourneyHistoryRequest.to(journeyHistoryRequest);
        journeyHistory.setJourney(journey);
        journeyHistoryRepository.save(journeyHistory);
        verifyCreationIntegration.createVerify(convertHelper.convertToVerifyCreationRequest(journeyHistory));

        return JourneyHistoryResponse.from(journeyHistory);
    }

    @Override
    @Transactional
    public JourneyHistoryResponse updateJourneyHistory(String id, UpdateStatusRequest request) {
        JourneyHistory journeyHistory = journeyHistoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Journey with id " + id + " not found")
        );
        if (request.getStatus() == Status.APPROVED) {
            _asyncJourney(journeyHistory);
        } else if (request.getStatus() == journeyHistory.getStatus()) {
            throw new ConflictException("This journey history status is not changed");
        }

        journeyHistory.setStatus(request.getStatus());
        journeyHistory.setNote(request.getNote());
        journeyHistoryRepository.save(journeyHistory);

        return JourneyHistoryResponse.from(journeyHistoryRepository.save(journeyHistory));
    }

    private void _asyncJourney(JourneyHistory journeyHistory) {
        Journey journey = journeyHistory.getJourney();

        // 🔹 1. Cập nhật thông tin hành trình mới nhất
        journey.setDistanceKm(journeyHistory.getNewDistance());
        journey.setAverageSpeed(journeyHistory.getAverageSpeed());
        journey.setEnergyUsed(journeyHistory.getEnergyUsed());

        // 🔹 2. Tính toán lượng CO₂ giảm phát thải
        double co2Reduced = JourneyUtils.calculateCo2Reduced(journey, journey.getVehicle().getVehicleType());
        journey.setCo2Reduced(co2Reduced);

//        // 🔹 3. Cập nhật tín chỉ carbon tương ứng
//        CarbonCredit existingCredit = carbonCreditRepository.findByJourneyId(journey.getId());
//
//        if (existingCredit == null) {
//            // ➕ Nếu chưa có → tạo mới tín chỉ carbon
//            CarbonCredit credit = CarbonCredit.builder()
//                    .journey(journey)
//                    .ownerId(journey.getVehicle().getOwnerId())
//                    .amount(co2Reduced)
//                    .status(CreditStatus.AVAILABLE)
//                    .build();
//            carbonCreditRepository.save(credit);
//
//        } else {
//            // ⚠️ Nếu đã có tín chỉ carbon
//            if (existingCredit.getStatus() == CreditStatus.AVAILABLE) {
//                // Có thể cập nhật nếu chưa giao dịch
//                existingCredit.setAmount(co2Reduced);
//                carbonCreditRepository.save(existingCredit);
//            } else {
//                // Đã giao dịch / rút — tạo adjustment để đảm bảo toàn vẹn
//                CarbonCredit adjustment = CarbonCredit.builder()
//                        .journey(journey)
//                        .ownerId(existingCredit.getOwnerId())
//                        .amount(co2Reduced - existingCredit.getAmount()) // phần chênh lệch
//                        .status(CreditStatus.ADJUSTMENT)
//                        .build();
//                carbonCreditRepository.save(adjustment);
//            }
//        }

        // 🔹 4. Lưu lại lịch sử hành trình
        journeyRepository.save(journey);
    }


    @Override
    public void deleteJourneyHistory(String id) {
        JourneyHistory journeyHistory = journeyHistoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Journey with id " + id + " not found")
        );
        journeyHistoryRepository.delete(journeyHistory);
    }
}
