package com.syos.application.ports.out;

import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.UserID;
import com.syos.domain.valueobjects.Username;
import com.syos.shared.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // Core CRUD operations
    Optional<User> findById(UserID userId);
    Optional<User> findByUsername(Username username);
    boolean existsByUsername(Username username);
    boolean existsByEmail(String email);
    User save(User user);
    void delete(User user);
    void deleteById(UserID userId);
    List<User> findAll();
    boolean existsById(UserID userId);
    long count();
    void deleteAll();
    
    // Legacy methods for backward compatibility with String parameters
    default Optional<User> findByUsername(String username) {
        return findByUsername(Username.of(username));
    }
    
    default boolean existsByUsername(String username) {
        return existsByUsername(Username.of(username));
    }
    
    default Optional<User> findById(Long id) {
        return findById(new UserID(id));
    }
    
    // Additional methods for Admin functionality
    default long countAll() { return count(); }
    default long countByRole(UserRole role) {
        return findAll().stream()
            .filter(user -> user.getRole() == role)
            .count();
    }
    
    default List<User> searchUsers(String searchTerm) {
        return findAll().stream()
            .filter(user -> 
                user.getUsername().getValue().toLowerCase().contains(searchTerm.toLowerCase()) ||
                user.getName().getValue().toLowerCase().contains(searchTerm.toLowerCase()) ||
                user.getEmail().getValue().toLowerCase().contains(searchTerm.toLowerCase())
            )
            .toList();
    }
}
