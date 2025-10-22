package com.example.commondto.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletCreationRequest {
    private String ownerId;
    private String email;       // Optional - để đối chiếu hoặc log
    private String source;      // Optional - ví dụ "USER_SERVICE"
}
