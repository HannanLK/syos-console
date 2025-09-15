package com.syos.application.ports.out;

import com.syos.domain.entities.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}
