package com.example.vehicleservice.controller;

import com.example.vehicleservice.model.dto.request.JourneyHistoryRequest;
import com.example.commondto.dto.request.UpdateStatusRequest;
import com.example.vehicleservice.model.dto.response.JourneyHistoryResponse;
import com.example.vehicleservice.model.filter.JourneyFilter;
import com.example.vehicleservice.service.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
@Tag(name = "Journey Management", description = "Manage journeys and journey histories")
public class JourneyController {

    private final JourneyService journeyService;

    // 🔹 Lấy danh sách lịch sử hành trình (có filter & phân trang)
    @GetMapping
    @Operation(summary = "Get all journey histories with filters")
    public ResponseEntity<List<JourneyHistoryResponse>> getAllJourneyHistory(JourneyFilter filter) {
        return ResponseEntity.ok(journeyService.getAllJourneyHistory(filter));
    }

//    // 🔹 Lấy thông tin 1 hành trình cụ thể
//    @GetMapping("/{id}")
//    @Operation(summary = "Get journey by id")
//    public ResponseEntity<JourneyResponse> getJourneyById(@PathVariable("id") String id) {
//        return ResponseEntity.ok(journeyService.getJourneyById(id));
//    }

    // 🔹 Lấy lịch sử của 1 hành trình cụ thể
    @GetMapping("/{id}")
    @Operation(summary = "Get journey history by journey id")
    public ResponseEntity<JourneyHistoryResponse> getJourneyHistoryByJourneyId(@PathVariable("id") String id) {
        return ResponseEntity.ok(journeyService.getJourneyHistoryByJourneyId(id));
    }

    // 🔹 Tạo mới lịch sử hành trình
    @PostMapping
    @Operation(summary = "Create journey history")
    public ResponseEntity<JourneyHistoryResponse> createJourneyHistory(
            @RequestBody JourneyHistoryRequest request
    ) {
        return ResponseEntity.ok(journeyService.createJourneyHistory(request));
    }

    // 🔹 Cập nhật lịch sử hành trình (PATCH + multipart/form-data)
    @PatchMapping(
            value = "/{id}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    @Operation(summary = "Update journey history (multipart/form-data)")
    public ResponseEntity<JourneyHistoryResponse> updateJourneyHistory(
            @PathVariable("id") String id,
            @ModelAttribute UpdateStatusRequest request
    ) throws IOException {


        return ResponseEntity.ok(journeyService.updateJourneyHistory(id, request));
    }

    // 🔹 Xóa lịch sử hành trình
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete journey history")
    public ResponseEntity<Void> deleteJourneyHistory(@PathVariable("id") String id) {
        journeyService.deleteJourneyHistory(id);
        return ResponseEntity.noContent().build();
    }

}
