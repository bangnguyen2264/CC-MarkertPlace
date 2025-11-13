package com.example.marketservice.service;

import com.example.commondto.constant.ListingStatus;
import com.example.marketservice.model.dto.request.MarketBidRequest;
import com.example.marketservice.model.dto.request.MarketPurchaseRequest;
import com.example.marketservice.model.dto.request.MarketListingRequest;
import com.example.marketservice.model.dto.response.MarketListingResponse;
import com.example.marketservice.model.filter.MarketListingFilter;

import java.util.List;

public interface MarketListingService {

    MarketListingResponse create(MarketListingRequest request);

    MarketListingResponse getById(String id);

    List<MarketListingResponse> getAll(MarketListingFilter marketListingFilter);

    MarketListingResponse update(String id, ListingStatus status);

    void delete(String id);

}
