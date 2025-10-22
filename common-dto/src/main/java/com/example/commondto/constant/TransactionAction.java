package com.example.commondto.constant;

/**
 * Các loại hành động giao dịch cho ví tiền và tín chỉ carbon.
 * Dùng chung cho bảng lịch sử (Audit ).
 */
public enum TransactionAction {

    // 💰 Ví tiền
    DEPOSIT,        // Nạp tiền vào ví
    WITHDRAW,       // Rút tiền ra khỏi ví


    // 🌱 Tín chỉ carbon
    CREDIT_TOP_UP,  // Nạp thêm tín chỉ carbon (
    CREDIT_TRADE,   // Bán tín chỉ carbon
    CREDIT_BUY,     // Mua tín chỉ carbon

    // ⚙️ Khác
    ADJUST_MANUAL   // Điều chỉnh thủ công (admin hoặc hệ thống)
}

