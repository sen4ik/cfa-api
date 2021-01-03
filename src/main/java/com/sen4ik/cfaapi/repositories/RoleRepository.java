package com.sen4ik.cfaapi.repositories;

import com.sen4ik.cfaapi.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findRoleByRoleName(String roleName);

}
