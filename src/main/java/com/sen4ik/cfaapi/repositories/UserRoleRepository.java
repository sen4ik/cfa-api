package com.sen4ik.cfaapi.repositories;

import com.sen4ik.cfaapi.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    List<UserRole> findRolesByUserId(int userId);

}
