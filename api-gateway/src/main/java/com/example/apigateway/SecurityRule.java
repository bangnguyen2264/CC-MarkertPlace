package com.example.apigateway;

import lombok.Data;

import java.util.List;

@Data
public class SecurityRule {
    private String path;              // đường dẫn (regex pattern)
    private List<String> methods;     // ["GET", "POST"] hoặc ["ANY"]
    private List<String> roles;       // ["PUBLIC"] hoặc ["USER", "ADMIN"]
}