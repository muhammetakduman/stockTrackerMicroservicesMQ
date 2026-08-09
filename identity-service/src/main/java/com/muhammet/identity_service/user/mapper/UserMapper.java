package com.muhammet.identity_service.user.mapper;

import com.muhammet.identity_service.role.entity.RoleName;
import com.muhammet.identity_service.user.dto.UserResponse;
import com.muhammet.identity_service.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<RoleName> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountLocked(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

