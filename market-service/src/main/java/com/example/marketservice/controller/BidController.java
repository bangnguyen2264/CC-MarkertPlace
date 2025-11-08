package com.example.marketservice.controller;

import com.example.marketservice.model.dto.request.MarketBidRequest;
import com.example.marketservice.model.dto.response.MarketBidResponse;
import com.example.marketservice.model.filter.BidFilter;
import com.example.marketservice.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bid")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<MarketBidResponse> create(@Valid @RequestBody  MarketBidRequest request) {
        return ResponseEntity.ok(bidService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketBidResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(bidService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MarketBidResponse>> getAll(@Valid @ParameterObject BidFilter filter) {
        return ResponseEntity.ok(bidService.getAll(filter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id")  String id) {
        bidService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
