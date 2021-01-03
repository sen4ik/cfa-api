package com.sen4ik.cfaapi.repositories;

import com.sen4ik.cfaapi.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findUserByEmail(String email);

}
