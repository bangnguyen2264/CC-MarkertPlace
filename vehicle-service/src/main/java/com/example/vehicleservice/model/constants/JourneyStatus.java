package com.example.vehicleservice.model.constants;

public enum JourneyStatus {
    PENDING,   // Người dùng gửi yêu cầu hành trình mới
    VERIFIED,  // CVA duyệt xác minh hành trình
    REJECTED   // CVA từ chối
}