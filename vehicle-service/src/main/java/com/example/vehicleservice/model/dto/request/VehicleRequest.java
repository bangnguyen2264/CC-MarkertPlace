package com.example.vehicleservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo hoặc cập nhật thông tin xe điện")
public class VehicleRequest {

    @Schema(description = "ID của người sở hữu xe (mapping với user bên User-Service)", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Owner ID không được để trống")
    private String ownerId;

    @Schema(description = "Mã VIN duy nhất của xe điện", example = "VF9A12345B6789012", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "VIN không được để trống")
    @Size(min = 10, max = 20, message = "VIN phải có độ dài từ 10 đến 20 ký tự")
    private String vin;

    @Schema(description = "Biển số xe điện", example = "30G-123.45", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
            regexp = "^[0-9A-Z]{2,3}-[0-9]{2,3}\\.[0-9]{2}$",
            message = "Biển số xe không hợp lệ, ví dụ hợp lệ: 30G-123.45"
    )
    private String licensePlate;

    @Schema(description = "Số đăng ký xe", example = "REG2025-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Số đăng ký xe không được để trống")
    private String registrationNumber;

    @Schema(description = "Màu sắc xe", example = "Trắng ngọc trai")
    @NotBlank(message = "Màu sắc xe không được để trống")
    private String color;

    @Schema(description = "Ngày đăng ký xe", example = "2023-10-01")
    @PastOrPresent(message = "Ngày đăng ký xe không được lớn hơn ngày hiện tại")
    private LocalDate registrationDate;

    @Schema(description = "Số km đã di chuyển (km)", example = "15200")
    @PositiveOrZero(message = "Số km phải lớn hơn hoặc bằng 0")
    private Long mileage;

    @Schema(description = "ID của loại xe (liên kết đến bảng vehicle_types)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Loaị xe không được để trống")
    private String vehicleTypeId;

    @Schema(description = "URL ảnh chụp giấy đăng ký xe", example = "https://cdn.example.com/vehicles/reg-001.jpg")
    @Pattern(
            regexp = "^(https?://).+",
            message = "URL ảnh đăng ký xe phải bắt đầu bằng http hoặc https"
    )
    private String registrationImageUrl;

    @Schema(description = "Ghi chú thêm về xe", example = "Xe còn mới, pin tốt, đã kiểm định tháng 9/2025")
    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}
