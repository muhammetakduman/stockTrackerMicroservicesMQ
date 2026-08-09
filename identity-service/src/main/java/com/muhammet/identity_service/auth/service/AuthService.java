package com.muhammet.identity_service.auth.service;

import com.muhammet.identity_service.auth.dto.*;
import com.muhammet.identity_service.exception.EmailAlreadyExistsException;
import com.muhammet.identity_service.exception.InvalidCredentialsException;
import com.muhammet.identity_service.exception.UserDisabledException;
import com.muhammet.identity_service.exception.UserLockedException;
import com.muhammet.identity_service.role.entity.Role;
import com.muhammet.identity_service.role.entity.RoleName;
import com.muhammet.identity_service.role.repository.RoleRepository;
import com.muhammet.identity_service.security.jwt.JwtService;
import com.muhammet.identity_service.security.principal.UserPrincipal;
import com.muhammet.identity_service.token.service.RefreshTokenService;
import com.muhammet.identity_service.user.dto.UserResponse;
import com.muhammet.identity_service.user.entity.User;
import com.muhammet.identity_service.user.mapper.UserMapper;
import com.muhammet.identity_service.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().strip();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        Role salesRole = roleRepository.findByName(RoleName.SALES_USER)
                .orElseThrow(() -> new IllegalStateException("SALES_USER role not found in database"));

        User user = new User();
        user.setFirstName(request.firstName().strip());
        user.setLastName(request.lastName().strip());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.addRole(salesRole);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", normalizedEmail);
        return userMapper.toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase().strip();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (DisabledException e) {
            log.warn("Login failed — account disabled: {}", normalizedEmail);
            throw new UserDisabledException();
        } catch (LockedException e) {
            log.warn("Login failed — account locked: {}", normalizedEmail);
            throw new UserLockedException();
        } catch (BadCredentialsException e) {
            log.warn("Login failed — invalid credentials for: {}", normalizedEmail);
            throw new InvalidCredentialsException();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByIdWithRoles(principal.getId())
                .orElseThrow(() -> new InvalidCredentialsException());

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in successfully: {}", normalizedEmail);
        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toResponse(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = refreshTokenService.getUserFromToken(request.refreshToken());
        User fullUser = userRepository.findByIdWithRoles(user.getId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!fullUser.isEnabled()) {
            throw new UserDisabledException();
        }
        if (fullUser.isAccountLocked()) {
            throw new UserLockedException();
        }

        UserPrincipal principal = UserPrincipal.from(fullUser);
        String newAccessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(request.refreshToken());

        log.info("Refresh token rotated for user: {}", fullUser.getEmail());
        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toResponse(fullUser)
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request.refreshToken());
        log.info("User logged out (refresh token revoked)");
    }
}

