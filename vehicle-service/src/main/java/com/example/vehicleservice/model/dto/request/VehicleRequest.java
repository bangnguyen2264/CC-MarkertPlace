package com.example.vehicleservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo hoặc cập nhật thông tin xe điện")
public class VehicleRequest {

    @Schema(description = "UUID của chủ sở hữu xe", example = "ce89d3f7-a3db-41e9-b237-8688d0ac5dc3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Mã chủ xe không được để trống")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "Mã chủ xe phải đúng định dạng UUID")
    private String ownerId;

    @Schema(description = "Mã VIN duy nhất của xe", example = "VF9A12345B6789012", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "VIN không được để trống")
    @Size(min = 10, max = 20, message = "VIN phải có độ dài từ 10 đến 20 ký tự")
    private String vin;

    @Schema(description = "Biển số xe", example = "30G-123.45", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
            regexp = "^[0-9A-Z]{2,3}-[0-9]{2,3}\\.[0-9]{2}$",
            message = "Biển số xe không hợp lệ, ví dụ hợp lệ: 30G-123.45"
    )
    private String licensePlate;

    @Schema(description = "Số đăng ký xe", example = "REG2025-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Số đăng ký xe không được để trống")
    @Size(max = 50, message = "Số đăng ký xe không được vượt quá 50 ký tự")
    private String registrationNumber;

    @Schema(description = "Màu sắc xe", example = "Trắng ngọc trai", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Màu sắc xe không được để trống")
    @Size(max = 30, message = "Tên màu không được vượt quá 30 ký tự")
    private String color;

    @Schema(description = "Ngày đăng ký xe", example = "2023-10-01")
    @PastOrPresent(message = "Ngày đăng ký xe không được lớn hơn ngày hiện tại")
    private LocalDate registrationDate;

    @Schema(description = "Số km đã di chuyển (km)", example = "15200")
    @PositiveOrZero(message = "Số km phải lớn hơn hoặc bằng 0")
    private Long mileage;

    @Schema(description = "UUID loại xe", example = "2fa74e8a-7ccf-4f85-a05d-63f0e23412c7")
    @NotBlank(message = "Loại xe không được để trống")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "Loại xe phải đúng định dạng UUID")
    private String vehicleTypeId;

    @Schema(description = "Danh sách URL ảnh giấy đăng ký xe")
    @Size(min = 1, message = "Phải có ít nhất một ảnh giấy đăng ký xe")
    private List<
            @Pattern(
                    regexp = "^(https?://).+",
                    message = "URL ảnh phải bắt đầu bằng http hoặc https"
            )
                    String
            > registrationImageUrl;

    @Schema(description = "Ghi chú thêm về xe", example = "Xe còn mới, pin tốt, đã kiểm định")
    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}
