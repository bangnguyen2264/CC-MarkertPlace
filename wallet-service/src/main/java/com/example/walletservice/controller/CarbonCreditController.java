package com.example.walletservice.controller;

import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.dto.response.CarbonCreditResponse;
import com.example.walletservice.model.filter.CarbonCreditFilter;
import com.example.walletservice.service.CarbonCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carbon-credit")
@RequiredArgsConstructor
public class CarbonCreditController {

    private final CarbonCreditService carbonCreditService;

    // 🟢 Lấy tất cả tín chỉ carbon (có hỗ trợ filter)
    @GetMapping
    public ResponseEntity<List<CarbonCreditResponse>> getAll(@Valid @ParameterObject CarbonCreditFilter filter) {
        return ResponseEntity.ok(carbonCreditService.getAll(filter));
    }

    // 🟢 Lấy tín chỉ carbon theo ID
    @GetMapping("/{id}")
    public ResponseEntity<CarbonCreditResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(carbonCreditService.getById(id));
    }

    // 🟢 Tạo mới ví tín chỉ carbon cho người dùng
    @PostMapping
    public ResponseEntity<CarbonCreditResponse> create(@RequestParam String ownerId) {
        return ResponseEntity.ok(carbonCreditService.create(ownerId));
    }

    // 🟢 Cập nhật số tín chỉ (nạp hoặc giao dịch)
    @PatchMapping(value ="/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarbonCreditResponse> update(
            @PathVariable String id,
            @ModelAttribute CarbonCreditUpdateRequest request
    ) {
        return ResponseEntity.ok(carbonCreditService.update(id, request));
    }
}
