package com.example.marketservice.controller;

import com.example.commondto.constant.ListingStatus;
import com.example.marketservice.model.dto.request.MarketListingRequest;
import com.example.marketservice.model.dto.request.MarketPurchaseRequest;
import com.example.marketservice.model.dto.response.MarketListingResponse;
import com.example.marketservice.model.filter.MarketListingFilter;
import com.example.marketservice.service.MarketListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listing")
@RequiredArgsConstructor
@Tag(name = "Market Listing API", description = "Quản lý danh sách niêm yết tín chỉ carbon")
public class MarketListingController {

    private final MarketListingService marketListingService;

    // ✅ Tạo mới niêm yết (fixed-price hoặc auction)
    @Operation(summary = "Tạo niêm yết mới")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarketListingResponse> createListing(@Valid @ModelAttribute MarketListingRequest request) {
        MarketListingResponse response = marketListingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Lấy danh sách niêm yết (có filter & paging)
    @Operation(summary = "Lấy danh sách niêm yết (có filter)")
    @GetMapping
    public ResponseEntity<List<MarketListingResponse>> getAllListings(@Valid @ModelAttribute @ParameterObject MarketListingFilter filter) {
        return ResponseEntity.ok(marketListingService.getAll(filter));
    }

    // ✅ Lấy chi tiết niêm yết
    @Operation(summary = "Lấy chi tiết niêm yết theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<MarketListingResponse> getListingById(@PathVariable("id") String id) {
        return ResponseEntity.ok(marketListingService.getById(id));
    }

    // ✅ Cập nhật niêm yết (chỉ seller hoặc admin)
    @Operation(summary = "Cập nhật thông tin niêm yết")
    @PatchMapping("/{id}")
    public ResponseEntity<MarketListingResponse> updateListing(
            @PathVariable("id") String id,
            @RequestParam("status") ListingStatus status
    ) {
        return ResponseEntity.ok(marketListingService.update(id, status));
    }

    // ✅ Xóa niêm yết (admin)
    @Operation(summary = "Xóa niêm yết (admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable String id) {
        marketListingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Mua ngay (Buy Now)
    @Operation(summary = "Mua ngay niêm yết giá cố định")
    @PostMapping("/purchase")
    public ResponseEntity<Void> purchase(
            @ParameterObject MarketPurchaseRequest request
    ) {
        marketListingService.purchase(request);
        return ResponseEntity.ok().build();
    }
}
