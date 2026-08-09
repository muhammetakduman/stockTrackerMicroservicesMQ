package com.muhammet.identity_service.role.repository;

import com.muhammet.identity_service.role.entity.Role;
import com.muhammet.identity_service.role.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);

    Set<Role> findAllByNameIn(Set<RoleName> names);
}

