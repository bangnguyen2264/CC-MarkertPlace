package com.example.userservice.controller;

import com.example.userservice.model.dto.request.ChangePasswordRequest;
import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;
import com.example.userservice.service.CustomerService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(customerService.getProfile());
    }

    @PatchMapping("/update")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @Schema(implementation = UserUpdateRequest.class) UserUpdateRequest request
    ){
        return ResponseEntity.ok(customerService.updateProfile(request));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(customerService.changePassword(request));
    }
}
