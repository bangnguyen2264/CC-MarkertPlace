package com.example.marketservice.service.impl;


import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import com.example.commondto.dto.request.CarbonCreditValidationRequest;
import com.example.commondto.dto.request.MarketPurchaseMessage;
import com.example.commondto.dto.request.UpdateCarbonCreditMessage;
import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.exception.BadRequestException;
import com.example.commondto.exception.ConflictException;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.marketservice.integration.CarbonCreditValidationIntegration;
import com.example.marketservice.kafka.producer.CarbonCreditProducer;
import com.example.marketservice.model.dto.request.MarketListingRequest;
import com.example.marketservice.model.dto.request.MarketPurchaseRequest;
import com.example.marketservice.model.dto.response.MarketBidResponse;
import com.example.marketservice.model.dto.response.MarketListingResponse;
import com.example.marketservice.model.entity.MarketListing;
import com.example.marketservice.model.filter.MarketListingFilter;
import com.example.marketservice.repository.BidRepository;
import com.example.marketservice.repository.MarketListingRepository;
import com.example.marketservice.service.MarketListingService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketListingServiceImpl implements MarketListingService {

    private final MarketListingRepository repository;
    private final BidRepository bidRepository;
    private final CarbonCreditProducer producer;
    private final CarbonCreditValidationIntegration validationIntegration;

    @Override
    public MarketListingResponse create(MarketListingRequest request) {
        _validateCredit(request);
        MarketListing listing;
        if (request.getType() == ListingType.FIXED_PRICE) {
            listing = MarketListing.builder()
                    .sellerId(request.getSellerId())
                    .creditId(request.getCreditId())
                    .pricePerCredit(request.getPricePerCredit())
                    .quantity(request.getQuantity())
                    .type(request.getType())
                    .status(ListingStatus.ACTIVE)
                    .endTime(request.getEndTime().toLocalDateTime())
                    .build();
        } else if (request.getType() == ListingType.AUCTION) {
            listing = MarketListing.builder()
                    .sellerId(request.getSellerId())
                    .creditId(request.getCreditId())
                    .pricePerCredit(request.getPricePerCredit())
                    .quantity(request.getQuantity())
                    .type(request.getType())
                    .status(ListingStatus.BIDDING)
                    .endTime(request.getEndTime().toLocalDateTime())
                    .build();
        } else {
            throw new BadRequestException("Invalid listing type");
        }
        producer.sendUpdateCarbonCreditRequest(UpdateCarbonCreditMessage.builder()
                .ownerId(request.getSellerId())
                .newTradedCredit(request.getQuantity())
                .build());
        return toResponse(repository.save(listing));
    }

    private void _validateCredit(MarketListingRequest request) {
        // 1️⃣ Gửi yêu cầu validate carbon credit
        CarbonCreditValidationRequest validateRequest = CarbonCreditValidationRequest.builder()
                .sellerId(request.getSellerId())
                .creditId(request.getCreditId())
                .quantity(request.getQuantity())// field này phải có trong request
                .build();
        CompletableFuture<CarbonCreditValidationResponse> future =
                validationIntegration.registerRequest(validateRequest.getCorrelationId());

        producer.sendValidateCarbonCreditRequest(validateRequest);
        log.info("📤 Sent carbon credit validation request with correlationId={}", validateRequest.getCorrelationId());

        try {
            // 2️⃣ Chờ phản hồi trong tối đa 10 giây
            CarbonCreditValidationResponse response = future.get(10, TimeUnit.SECONDS);

            if (!response.isSuccess()) {
                log.warn("❌ Validation failed for correlationId={}, reason={}",
                        validateRequest.getCorrelationId(), response.getMessage());
                throw new IllegalStateException("Carbon credit validation failed: " + response.getMessage());
            }

            log.info("✅ Validation succeeded for correlationId={}", validateRequest.getCorrelationId());
        } catch (Exception e) {
            log.error("⚠️ Validation timeout or error for correlationId={}", validateRequest.getCorrelationId(), e);
            validationIntegration.remove(validateRequest.getCorrelationId());
            throw new IllegalStateException("Failed to validate carbon credit before creating listing.");
        }
    }

    @Override
    public MarketListingResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Listing not found with id: " + id));
    }

    @Override
    public List<MarketListingResponse> getAll(MarketListingFilter marketListingFilter) {
        Pageable pageable = CrudUtils.createPageable(marketListingFilter);
        Specification<MarketListing> specification = _buildFilter(marketListingFilter);
        Page<MarketListing> result = repository.findAll(specification, pageable);
        return result.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Specification<MarketListing> _buildFilter(MarketListingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- ID filter ---
            if (filter.getSellerId() != null && !filter.getSellerId().isEmpty()) {
                predicates.add(cb.equal(root.get("sellerId"), filter.getSellerId()));
            }
            if (filter.getCreditId() != null && !filter.getCreditId().isEmpty()) {
                predicates.add(cb.equal(root.get("creditId"), filter.getCreditId()));
            }

            // --- Enum filter ---
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // --- Price range ---
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerCredit"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerCredit"), filter.getMaxPrice()));
            }

            // --- Quantity range ---
            if (filter.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), filter.getMinQuantity()));
            }
            if (filter.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), filter.getMaxQuantity()));
            }

            // --- Date range ---
            if (filter.getStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("startTime"),
                        filter.getStartFrom().toLocalDateTime()
                ));
            }
            if (filter.getEndBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("endTime"),
                        filter.getEndBefore().toLocalDateTime()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    @Override
    @Transactional
    public MarketListingResponse update(String id, ListingStatus newStatus) {
        MarketListing listing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found with id: " + id));

        // 1️⃣ Validate trạng thái hiện tại
        if (isFinalStatus(listing.getStatus())) {
            throw new BadRequestException("Cannot modify listing already " + listing.getStatus());
        }

        // 2️⃣ Validate yêu cầu cập nhật
        if (newStatus == ListingStatus.ACTIVE) {
            throw new ConflictException("Cannot revert listing to ACTIVE");
        }

        // 3️⃣ Cập nhật trạng thái listing
        listing.setStatus(newStatus);
        MarketListing saved = repository.save(listing);

        // 4️⃣ Gửi message bất đồng bộ (sau khi commit DB)
        publishStatusChange(saved);

        return toResponse(saved);
    }

    /**
     * Kiểm tra nếu trạng thái đã là “chốt” (không được thay đổi).
     */
    private boolean isFinalStatus(ListingStatus status) {
        return status == ListingStatus.SOLD
                || status == ListingStatus.EXPIRED
                || status == ListingStatus.CANCELED;
    }

    /**
     * Gửi Kafka message tương ứng theo trạng thái listing.
     */
    private void publishStatusChange(MarketListing listing) {
        switch (listing.getStatus()) {
            case CANCELED -> producer.sendUpdateCarbonCreditRequest(
                    UpdateCarbonCreditMessage.builder()
                            .ownerId(listing.getSellerId())
                            .newTradedCredit(-listing.getQuantity())
                            .build()
            );

            case SOLD -> producer.sendUpdateCarbonCreditRequest(
                    UpdateCarbonCreditMessage.builder()
                            .ownerId(listing.getSellerId())
                            .newTradedCredit(listing.getQuantity())
                            .build()
            );

            case EXPIRED -> log.info("Listing {} expired automatically.", listing.getId());
        }
    }


    @Override
    public void delete(String id) {
        MarketListing listing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found with id: " + id));

        if (listing.getStatus() == ListingStatus.SOLD || listing.getStatus() == ListingStatus.EXPIRED) {
            throw new BadRequestException("Cannot delete sold or expired listing");
        }
        repository.delete(listing);
    }



    private MarketListingResponse toResponse(MarketListing entity) {
        return MarketListingResponse.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .pricePerCredit(entity.getPricePerCredit())
                .quantity(entity.getQuantity())
                .bidResponseList(
                        entity.getBids() == null
                                ? List.of() // danh sách rỗng
                                : entity.getBids().stream()
                                .map(MarketBidResponse::from)
                                .collect(Collectors.toList())
                )
                .type(entity.getType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .endTime(entity.getEndTime())
                .build();
    }

}
