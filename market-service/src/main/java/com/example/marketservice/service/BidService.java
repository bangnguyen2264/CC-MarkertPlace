package com.example.marketservice.service;

import com.example.marketservice.model.dto.request.MarketBidRequest;
import com.example.marketservice.model.dto.response.MarketBidResponse;
import com.example.marketservice.model.filter.BidFilter;

import java.util.List;

public interface BidService {
    MarketBidResponse create(MarketBidRequest request);

    MarketBidResponse getById(String id);

    List<MarketBidResponse> getAll(BidFilter filter);

    void delete(String id);
}