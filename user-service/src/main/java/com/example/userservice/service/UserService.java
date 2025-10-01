package com.example.userservice.service;

import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;
import com.example.userservice.model.filter.UserFilter;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll(UserFilter userFilter);
    UserResponse getById(Long id);
    UserResponse update(Long id,UserUpdateRequest userUpdateRequest);
    void delete(Long id);
}
