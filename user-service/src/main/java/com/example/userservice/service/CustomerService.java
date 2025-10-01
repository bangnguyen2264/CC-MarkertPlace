package com.example.userservice.service;

import com.example.userservice.model.dto.request.ChangePasswordRequest;
import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;

public interface CustomerService {
    UserResponse getProfile();
    UserResponse updateProfile(UserUpdateRequest user);
    String changePassword(ChangePasswordRequest changePasswordRequest);
}
