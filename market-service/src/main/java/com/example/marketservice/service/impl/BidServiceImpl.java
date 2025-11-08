package com.example.marketservice.service.impl;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import com.example.commondto.exception.BadRequestException;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.marketservice.model.dto.request.MarketBidRequest;
import com.example.marketservice.model.dto.response.MarketBidResponse;
import com.example.marketservice.model.entity.Bid;
import com.example.marketservice.model.entity.MarketListing;
import com.example.marketservice.model.filter.BidFilter;
import com.example.marketservice.repository.BidRepository;
import com.example.marketservice.repository.MarketListingRepository;
import com.example.marketservice.service.BidService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final MarketListingRepository listingRepository;

    @Override
    @Transactional
    public MarketBidResponse create(MarketBidRequest request) {
        MarketListing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new NotFoundException("Listing not found with id: " + request.getListingId()));

        if (listing.getType() != ListingType.AUCTION) {
            throw new BadRequestException("This listing is not an auction");
        }
        if (listing.getStatus() != ListingStatus.BIDDING) {
            throw new BadRequestException("Auction is not open for bidding");
        }

        if (listing.getEndTime() != null && listing.getEndTime().isBefore(LocalDateTime.now())) {
            listing.setStatus(ListingStatus.EXPIRED);
            listingRepository.save(listing);
            throw new BadRequestException("Auction has expired");
        }

        double currentHighest = Optional.ofNullable(listing.getHighestBid())
                .orElse(Optional.ofNullable(listing.getStartingPrice()).orElse(0.0));

        if (request.getBidAmount() <= currentHighest) {
            throw new BadRequestException("Bid amount must be higher than current highest bid (" + currentHighest + ")");
        }

        // ✅ Cập nhật listing
        listing.setHighestBid(request.getBidAmount());
        listing.setHighestBidderId(request.getBidderId());
        listingRepository.save(listing);

        // ✅ Lưu Bid
        Bid bid = Bid.builder()
                .listing(listing)
                .bidderId(request.getBidderId())
                .bidderName(request.getBidderName())
                .amount(request.getBidAmount())
                .build();
        bidRepository.save(bid);

        log.info("🏷️ New bid placed: bidder={} amount={} listing={}",
                request.getBidderId(), request.getBidAmount(), request.getListingId());

        return MarketBidResponse.from(bid);
    }


    @Override
    public MarketBidResponse getById(String id) {
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bid not found with id: " + id));
        return MarketBidResponse.from(bid);
    }

    @Override
    public List<MarketBidResponse> getAll(BidFilter filter) {
        Pageable pageable = CrudUtils.createPageable(filter);
        Specification<Bid> spec = _buildFilter(filter);

        Page<Bid> page = bidRepository.findAll(spec, pageable);
        return page.stream().map(MarketBidResponse::from).collect(Collectors.toList());
    }

    private Specification<Bid> _buildFilter(BidFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getListingId() != null) {
                predicates.getExpressions().add(cb.equal(root.get("listing").get("id"), filter.getListingId()));
            }
            if (filter.getBidderId() != null) {
                predicates.getExpressions().add(cb.equal(root.get("bidderId"), filter.getBidderId()));
            }
            if (filter.getMinAmount() != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }
            if (filter.getStartFrom() != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartFrom()));
            }
            if (filter.getEndBefore() != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndBefore()));
            }

            return predicates;
        };
    }

    @Override
    public void delete(String id) {
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bid not found with id: " + id));
        bidRepository.delete(bid);
        log.info("🗑️ Deleted bid {}", id);
    }
}
