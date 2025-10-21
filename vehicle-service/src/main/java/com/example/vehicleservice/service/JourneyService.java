package com.example.vehicleservice.service;

import com.example.vehicleservice.model.dto.request.JourneyHistoryRequest;
import com.example.vehicleservice.model.dto.request.UpdateJourneyHistoryRequest;
import com.example.vehicleservice.model.dto.response.JourneyHistoryResponse;
import com.example.vehicleservice.model.dto.response.JourneyResponse;
import com.example.vehicleservice.model.filter.JourneyFilter;

import java.util.List;

public interface JourneyService {
    List<JourneyHistoryResponse> getAllJourneyHistory(JourneyFilter journeyFilter);
    JourneyResponse getJourneyById(String id);
    JourneyHistoryResponse getJourneyHistoryByJourneyId(String id);
    JourneyHistoryResponse createJourneyHistory(  JourneyHistoryRequest journeyHistoryRequest);
    JourneyHistoryResponse updateJourneyHistory(String id,UpdateJourneyHistoryRequest request);
    void deleteJourneyHistory(String id);

}
