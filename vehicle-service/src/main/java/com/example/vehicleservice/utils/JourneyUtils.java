package com.example.vehicleservice.utils;

import com.example.vehicleservice.model.entity.Journey;
import com.example.vehicleservice.model.entity.VehicleType;

/**
 * Tiện ích tính toán lượng CO₂ giảm phát thải của chuyến đi.
 * Dùng để đồng bộ dữ liệu từ JourneyHistory sang Journey.
 */
public class JourneyUtils {

    // 🔹 Hằng số giả định (dựa trên nghiên cứu năng lượng và khí thải trung bình)
    private static final double EF_ELECTRIC = 0.4;     // kg CO₂ / kWh — hệ số phát thải điện lưới trung bình toàn cầu
    private static final double OPTIMAL_SPEED = 60.0;  // km/h — tốc độ tối ưu giúp xe điện tiêu thụ năng lượng hiệu quả nhất
    private static final double SPEED_FACTOR = 0.1;    // 10% — hệ số ảnh hưởng của tốc độ (quá nhanh hoặc quá chậm làm giảm hiệu suất)

    /**
     * Tính lượng CO₂ giảm được khi xe điện di chuyển thay cho xe chạy nhiên liệu.
     *
     * @param journey     thông tin chuyến đi (bao gồm quãng đường, tốc độ trung bình, năng lượng tiêu hao)
     * @param vehicleType loại xe (chứa thông tin CO₂ phát thải/km của xe nhiên liệu tương đương)
     * @return Lượng CO₂ giảm phát thải (đơn vị: kg)
     */
    public static double calculateCo2Reduced(Journey journey, VehicleType vehicleType) {
        if (journey == null || vehicleType == null) {
            throw new IllegalArgumentException("Journey or VehicleType cannot be null");
        }

        Double distanceKm = journey.getDistanceKm();
        Double avgSpeed = journey.getAverageSpeed();
        Double energyUsedKWh = journey.getEnergyUsed();

        if (distanceKm == null || avgSpeed == null || energyUsedKWh == null) {
            throw new IllegalArgumentException("Journey data is incomplete (distance, speed, or energy missing)");
        }

        if (distanceKm <= 0 || avgSpeed <= 0 || energyUsedKWh <= 0) {
            throw new IllegalArgumentException("Journey data must contain positive values");
        }

        double co2PerKmFuel = vehicleType.getCo2PerKm();

        // 🔹 Tính toán hệ số hiệu suất tốc độ
        double speedFactor = 1 - Math.abs(avgSpeed - OPTIMAL_SPEED) / OPTIMAL_SPEED * SPEED_FACTOR;

        // 🔹 Tính lượng CO₂ giảm phát thải:
        //   CO2 giảm = (CO2 phát thải nếu dùng xe xăng) - (CO2 từ năng lượng điện)
        double co2Reduced = (distanceKm * co2PerKmFuel) - (energyUsedKWh * EF_ELECTRIC * speedFactor);

        // 🔹 Nếu kết quả âm (xe điện tiêu hao năng lượng nhiều hơn mức tiết kiệm được)
        // thì trả về 0 để tránh giá trị âm
        return Math.max(co2Reduced, 0);
    }
}
