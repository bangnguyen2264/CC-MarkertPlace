package com.example.walletservice.model.entity;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ID chủ sở hữu ví hoặc tín chỉ
    @Column(nullable = false)
    private String ownerId;

    // Loại giao dịch: WALLET hoặc CARBON_CREDIT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // Hành động: DEPOSIT, WITHDRAW, TRADE, ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionAction action;

    // Số lượng hoặc giá trị giao dịch
    @Column(nullable = false)
    private Double amount;

    // Số dư hoặc tín chỉ còn lại sau giao dịch
    private Double balanceAfter;

    // Mô tả thêm (VD: "Nạp tiền từ Momo", "Mua tín chỉ carbon")
    private String description;

    // Tham chiếu đến giao dịch khác (nếu cần, VD: id của CarbonCredit trade)
    private String referenceId;
}
