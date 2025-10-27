package com.example.walletservice.controller;

import com.example.walletservice.model.dto.request.WalletUpdateRequest;
import com.example.walletservice.model.dto.response.WalletResponse;
import com.example.walletservice.model.filter.WalletFilter;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // 🟢 Lấy danh sách ví (hỗ trợ filter + phân trang)
    @GetMapping
    public ResponseEntity<List<WalletResponse>> getAll(@Valid @ParameterObject WalletFilter filter) {
        return ResponseEntity.ok(walletService.getAll(filter));
    }

    // 🟢 Lấy ví theo ID
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(walletService.getById(id));
    }

    // 🟢 Tạo ví mới cho user
    @PostMapping
    public ResponseEntity<WalletResponse> create(@RequestParam String ownerId) {
        return ResponseEntity.ok(walletService.create(ownerId));
    }

    // 🟢 Cập nhật số dư ví (nạp hoặc rút)
    @PatchMapping(value ="/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WalletResponse> update(
            @PathVariable("id") String id,
            @ModelAttribute WalletUpdateRequest request
    ) {
        return ResponseEntity.ok(walletService.update(id, request));
    }
}
