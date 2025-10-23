package com.example.walletservice.model.filter;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.dto.filter.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class AuditFilter extends BaseFilter {
    private String ownerId;
    private TransactionType type;
    private TransactionAction action;
    private String referenceId;
}
