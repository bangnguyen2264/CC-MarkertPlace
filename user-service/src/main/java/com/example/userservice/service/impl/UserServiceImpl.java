package com.example.userservice.service.impl;

import com.example.userservice.exception.NotFoundException;
import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;
import com.example.userservice.model.entity.User;
import com.example.userservice.model.filter.UserFilter;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll(UserFilter userFilter) {
        // Create sorting
        Sort sort = Sort.by(userFilter.getSort(),
                userFilter.getField() != null ? userFilter.getField() : "id");

        // Create pageable
        Pageable pageable = PageRequest.of(userFilter.getPage(), userFilter.getEntry(), sort);

        // Build specification for filtering
        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userFilter.getFullName() != null && !userFilter.getFullName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")),
                        "%" + userFilter.getFullName().toLowerCase() + "%"));
            }
            if (userFilter.getEmail() != null && !userFilter.getEmail().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + userFilter.getEmail().toLowerCase() + "%"));
            }
            if (userFilter.getPhoneNumber() != null && !userFilter.getPhoneNumber().isBlank()) {
                predicates.add(cb.like(root.get("phoneNumber"),
                        "%" + userFilter.getPhoneNumber() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Fetch data from DB
        Page<User> userPage = userRepository.findAll(specification, pageable);

        // Convert to DTO
        return userPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponseDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse update(Long id,UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setFullName(userUpdateRequest.getFullName());
        user.setEmail(userUpdateRequest.getEmail());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        user.setDob(userUpdateRequest.getDob());

        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponseDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .role(user.getRole())
                .build();
    }
}