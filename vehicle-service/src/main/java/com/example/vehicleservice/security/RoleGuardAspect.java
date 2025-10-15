package com.example.vehicleservice.security;


import com.example.vehicleservice.exception.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class RoleGuardAspect {

    @Before("@within(roleRequired) || @annotation(roleRequired)")
    public void authorize(JoinPoint joinPoint, RoleRequired roleRequired) {
        var context = UserContextHolder.get();

        if (context == null) {
            throw new AccessDeniedException("Missing user context");
        }

        String currentRole = context.getRole();
        if (currentRole == null || currentRole.isEmpty()) {
            throw new AccessDeniedException("No role found in context");
        }

        boolean allowed = Arrays.stream(roleRequired.value())
                .anyMatch(role -> role.equalsIgnoreCase(currentRole));

        if (!allowed) {
            log.warn("Access denied: user '{}' with role '{}' tried to access {}",
                    context.getUsername(), currentRole, joinPoint.getSignature());
            throw new AccessDeniedException("You cannot access by your role ");
        }
    }
}
