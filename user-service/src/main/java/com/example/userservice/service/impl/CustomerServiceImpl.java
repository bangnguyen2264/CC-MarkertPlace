package com.example.userservice.service.impl;

import com.example.commondto.utils.BeanCopyUtils;
import com.example.userservice.exception.UnauthorizedException;
import com.example.userservice.model.dto.request.ChangePasswordRequest;
import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;
import com.example.userservice.model.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.CustomerService;
import com.example.userservice.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse getProfile() {
        return UserResponse.from(getCurrentUser());
    }

    @Override
    public UserResponse updateProfile(UserUpdateRequest user) {
        User userToUpdate = getCurrentUser();

        try {
            BeanCopyUtils.copyNonNullProperties(user, userToUpdate);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
        return UserResponse.from( userRepository.save(userToUpdate));
    }

    @Override
    public String changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid old password");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }

    private User getCurrentUser(){
        return userRepository.findByEmail(UserUtils.getMe()).orElseThrow(
                () -> new AuthenticationException("<UNK> <UNK> <UNK> <UNK> <UNK> <UNK>") {
                }
        );
    }
}
