package com.example.vehicleservice.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoleRequired {
    String[] value(); // Danh sách role được phép
}
