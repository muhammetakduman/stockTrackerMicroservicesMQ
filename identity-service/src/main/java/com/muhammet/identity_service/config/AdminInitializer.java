package com.muhammet.identity_service.config;

import com.muhammet.identity_service.role.entity.Role;
import com.muhammet.identity_service.role.entity.RoleName;
import com.muhammet.identity_service.role.repository.RoleRepository;
import com.muhammet.identity_service.user.entity.User;
import com.muhammet.identity_service.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds roles and creates a default admin user on startup.
 * Idempotent — safe to run multiple times.
 */
@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {

    @Value("${admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${admin.password:Admin123!@#}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Role created: {}", roleName);
            }
        }
    }

    private void seedAdminUser() {
        String normalizedEmail = adminEmail.toLowerCase().strip();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Admin user already exists, skipping.");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.addRole(adminRole);

        userRepository.save(admin);
        log.info("Default admin user created: {}", normalizedEmail);
    }
}



