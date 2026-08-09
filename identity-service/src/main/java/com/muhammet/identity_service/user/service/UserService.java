package com.muhammet.identity_service.user.service;

import com.muhammet.identity_service.exception.RoleNotFoundException;
import com.muhammet.identity_service.exception.UserNotFoundException;
import com.muhammet.identity_service.role.entity.Role;
import com.muhammet.identity_service.role.entity.RoleName;
import com.muhammet.identity_service.role.repository.RoleRepository;
import com.muhammet.identity_service.user.dto.UpdateRolesRequest;
import com.muhammet.identity_service.user.dto.UpdateStatusRequest;
import com.muhammet.identity_service.user.dto.UserResponse;
import com.muhammet.identity_service.user.entity.User;
import com.muhammet.identity_service.user.mapper.UserMapper;
import com.muhammet.identity_service.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            // Eagerly load roles within the transaction
            user.getRoles().size();
            return userMapper.toResponse(user);
        });
    }

    @Transactional
    public UserResponse updateStatus(UUID userId, UpdateStatusRequest request) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setEnabled(request.enabled());
        userRepository.save(user);

        log.info("Admin updated user {} enabled={}", userId, request.enabled());
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateRoles(UUID userId, UpdateRolesRequest request) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Set<Role> newRoles = request.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException(roleName.name())))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);
        userRepository.save(user);

        Set<RoleName> assignedRoles = newRoles.stream().map(Role::getName).collect(Collectors.toSet());
        log.info("Admin updated roles for user {} to {}", userId, assignedRoles);
        return userMapper.toResponse(user);
    }
}

