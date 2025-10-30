package com.example.verificationservice.verify_request;

import com.example.commondto.constant.Status;
import com.example.commondto.dto.request.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verify-requests")
@RequiredArgsConstructor
public class VerifyRequestController {

    private final VerifyRequestService verifyRequestService;

    /**
     * 📄 Lấy danh sách verify request có filter (phân trang, tìm kiếm, lọc)
     */
    @GetMapping
    public List<VerifyRequest> getAll(@Valid @ParameterObject VerifyRequestFilter filter) {
        return verifyRequestService.getAll(filter);
    }

    /**
     * 🔍 Lấy chi tiết một verify request theo ID
     */
    @GetMapping("/{id}")
    public VerifyRequest getById(@Parameter(description = "Verify request ID", required = true)
                                 @PathVariable("id") String id) {
        return verifyRequestService.getById(id);
    }

    /**
     * 📨 Gửi yêu cầu xác minh mới (ví dụ khi user đăng xe, hành trình...)
     */
    @PostMapping
    public VerifyRequest create(@RequestBody VerifyCreationDto request) {
        return verifyRequestService.create(request);
    }

    /**
     * ✅ Cập nhật trạng thái yêu cầu (duyệt / từ chối)
     */
    @PatchMapping(
            value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public VerifyRequest updateStatus(
            @PathVariable("id") String id,
            @ModelAttribute UpdateStatusRequest request
    ) {
        return verifyRequestService.update(id, request);
    }
}
