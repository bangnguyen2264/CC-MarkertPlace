package com.example.userservice.service.impl;

import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.BeanCopyUtils;
import com.example.userservice.model.dto.request.UserUpdateRequest;
import com.example.userservice.model.dto.response.UserResponse;
import com.example.userservice.model.entity.User;
import com.example.userservice.model.filter.UserFilter;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // -----------------------------
// GET ALL (Không dùng cache)
// -----------------------------
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll(UserFilter userFilter) {

        Sort sort = Sort.by(userFilter.getSort(),
                userFilter.getField() != null ? userFilter.getField() : "id");

        Pageable pageable = PageRequest.of(userFilter.getPage(), userFilter.getEntry(), sort);

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

        Page<User> userPage = userRepository.findAll(specification, pageable);

        return userPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // -----------------------------
// GET USER BY ID (Uses cache)
// -----------------------------
    @Override
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Cacheable(value = "userById", key = "'user:' + #id")
    public UserResponse getById(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return mapToResponseDto(user);
    }

    // -----------------------------
// UPDATE USER (Evict cache by ID)
// -----------------------------
    @Override
    @CacheEvict(value = "userById", key = "'user:' + #id")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse update(String id, UserUpdateRequest userUpdateRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        try {
            BeanCopyUtils.copyNonNullProperties(userUpdateRequest, user);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }

        return mapToResponseDto(userRepository.save(user));
    }

    // -----------------------------
// DELETE USER (Evict cache)
// -----------------------------
    @Override
    @CacheEvict(value = "userById", key = "'user:' + #id")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    // -----------------------------
// MAP ENTITY TO DTO
// -----------------------------
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
