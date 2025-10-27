package com.example.commondto.dto.response;

import com.example.commondto.dto.request.BaseMessageKafka;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WalletCreationResponse extends BaseMessageKafka {
    private String ownerId;
    private boolean success;
    private String message;
}
