package com.example.commondto.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WalletCreationRequest extends BaseMessageKafka{
    private String ownerId;
    private String email;       // Optional - để đối chiếu hoặc log
    private String source;      // Optional - ví dụ "USER_SERVICE"
}
