package com.example.walletservice.service;

import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.dto.response.CarbonCreditResponse;
import com.example.walletservice.model.filter.CarbonCreditFilter;

import java.util.List;

public interface CarbonCreditService {
    List<CarbonCreditResponse> getAll(CarbonCreditFilter carbonCreditFilter);
    CarbonCreditResponse getById(String id);
    CarbonCreditResponse create(String ownerId);
    CarbonCreditResponse update(String id, CarbonCreditUpdateRequest carbonCreditUpdateRequest);
}
