package com.example.apigateway;

import lombok.Data;

@Data
public class SecurityRule {
    private String path;     // đường dẫn
    private String method;   // GET, POST, ANY,...
    private String access;   // PUBLIC, USER, ADMIN,...
}
