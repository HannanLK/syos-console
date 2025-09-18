package com.syos.application.ports.out;

import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    
    // Additional methods for Admin functionality
    List<User> findAll();
    long countAll();
    long countByRole(UserRole role);
    List<User> searchUsers(String searchTerm);
    Optional<User> findById(Long id);
}
