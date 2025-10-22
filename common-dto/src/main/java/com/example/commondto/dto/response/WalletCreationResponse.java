package com.example.commondto.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreationResponse {
    private String ownerId;
    private boolean success;
    private String message;
}
