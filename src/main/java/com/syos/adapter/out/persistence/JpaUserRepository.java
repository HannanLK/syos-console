package com.syos.adapter.out.persistence;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA implementation of UserRepository using PostgreSQL
 */
public class JpaUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(JpaUserRepository.class);
    private final EntityManagerFactory entityManagerFactory;

    public JpaUserRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<com.syos.infrastructure.persistence.UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE LOWER(u.username) = LOWER(:username)", 
                com.syos.infrastructure.persistence.UserEntity.class
            );
            query.setParameter("username", username);
            
            UserEntity userEntity = query.getSingleResult();
            User user = mapToDomain(userEntity);
            logger.debug("Found user by username: {}", username);
            return Optional.of(user);
            
        } catch (NoResultException e) {
            logger.debug("User not found by username: {}", username);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM UserEntity u WHERE LOWER(u.username) = LOWER(:username)", 
                Long.class
            );
            query.setParameter("username", username);
            
            Long count = query.getSingleResult();
            boolean exists = count > 0;
            logger.debug("Username '{}' exists: {}", username, exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Error checking username existence: {}", username, e);
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM UserEntity u WHERE LOWER(u.email) = LOWER(:email)", 
                Long.class
            );
            query.setParameter("email", email);
            
            Long count = query.getSingleResult();
            boolean exists = count > 0;
            logger.debug("Email '{}' exists: {}", email, exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", email, e);
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public User save(User user) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            
            UserEntity userEntity;
            User savedUser;
            
            if (user.getId() == null) {
                // New user - let database generate ID
                userEntity = mapToEntity(user);
                userEntity.setId(null); // Let DB generate
                em.persist(userEntity);
                em.flush(); // Force ID generation
                
                // Create User with the generated ID
                savedUser = mapToDomain(userEntity);
                
                logger.info("Saved new user: {} with role: {}", 
                    user.getUsername().getValue(), user.getRole());
            } else {
                // Update existing user
                userEntity = em.find(UserEntity.class, user.getId().getValue());
                if (userEntity != null) {
                    updateEntity(userEntity, user);
                    em.merge(userEntity);
                    savedUser = user; // Return the original user for updates
                    logger.info("Updated existing user: {}", user.getUsername().getValue());
                } else {
                    // User with ID doesn't exist, create new
                    userEntity = mapToEntity(user);
                    em.persist(userEntity);
                    savedUser = user;
                    logger.info("Saved user with preset ID: {}", user.getId().getValue());
                }
            }
            
            em.getTransaction().commit();
            return savedUser;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error saving user: {}", user.getUsername().getValue(), e);
            throw new RuntimeException("Failed to save user", e);
        } finally {
            em.close();
        }
    }

    private UserEntity mapToEntity(User user) {
        UserEntity entity = new UserEntity();
        updateEntity(entity, user);
        return entity;
    }

    private void updateEntity(UserEntity entity, User user) {
        if (user.getId() != null) {
            entity.setId(user.getId().getValue());
        }
        entity.setUsername(user.getUsername().getValue());
        entity.setPasswordHash(user.getPassword().getHash());
        entity.setRole(user.getRole());
        entity.setName(user.getName().getValue());
        entity.setEmail(user.getEmail().getValue());
        entity.setSynexPoints(user.getSynexPoints().getValue());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(LocalDateTime.now());
        if (user.getCreatedBy() != null) {
            entity.setCreatedBy(user.getCreatedBy().getValue());
        }
        // MemberSince not tracked in domain now; align to createdAt for compatibility
        entity.setMemberSince(user.getCreatedAt());
    }

    private User mapToDomain(UserEntity entity) {
        return User.reconstitute(
            UserID.of(entity.getId()),
            Username.of(entity.getUsername()),
            Password.fromHash(entity.getPasswordHash()),
            entity.getRole(),
            Name.of(entity.getName()),
            Email.of(entity.getEmail()),
            SynexPoints.of(entity.getSynexPoints()),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy() != null ? UserID.of(entity.getCreatedBy()) : null
        );
    }

    // ===== Implementations for extended UserRepository contract =====
    @Override
    public Optional<User> findById(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            UserEntity entity = em.find(UserEntity.class, id);
            return entity != null ? Optional.of(mapToDomain(entity)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<User> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<UserEntity> query = em.createQuery("SELECT u FROM UserEntity u", UserEntity.class);
            java.util.List<UserEntity> entities = query.getResultList();
            java.util.List<User> users = new java.util.ArrayList<>();
            for (UserEntity e : entities) {
                users.add(mapToDomain(e));
            }
            return users;
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return java.util.Collections.emptyList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting users", e);
            return 0L;
        } finally {
            em.close();
        }
    }

    @Override
    public long countByRole(com.syos.shared.enums.UserRole role) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.role = :role", Long.class);
            query.setParameter("role", role);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting users by role", e);
            return 0L;
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<User> searchUsers(String searchTerm) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            String term = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
            jakarta.persistence.TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKE :term OR LOWER(u.email) LIKE :term OR LOWER(u.name) LIKE :term",
                UserEntity.class
            );
            query.setParameter("term", "%" + term + "%");
            java.util.List<UserEntity> entities = query.getResultList();
            java.util.List<User> users = new java.util.ArrayList<>();
            for (UserEntity e : entities) {
                users.add(mapToDomain(e));
            }
            return users;
        } catch (Exception e) {
            logger.error("Error searching users", e);
            return java.util.Collections.emptyList();
        } finally {
            em.close();
        }
    }
    
    public int getUserCount() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.error("Error counting users", e);
            return 0;
        } finally {
            em.close();
        }
    }
    
    public void printAllUsers() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<com.syos.infrastructure.persistence.UserEntity> query = em.createQuery("SELECT u FROM UserEntity u", com.syos.infrastructure.persistence.UserEntity.class);
            var users = query.getResultList();
            
            logger.info("=== All Users in Database Repository ===");
            logger.info("Total users: {}", users.size());
            
            for (UserEntity user : users) {
                logger.info("ID: {}, Username: {}, Email: {}, Role: {}", 
                    user.getId(), user.getUsername(), user.getEmail(), user.getRole());
            }
            logger.info("============================");
        } catch (Exception e) {
            logger.error("Error printing users", e);
        } finally {
            em.close();
        }
    }

    // Missing methods from UserRepository interface
    @Override
    public Optional<User> findById(UserID userId) {
        if (userId == null || userId.getValue() == null) return Optional.empty();
        return findById(userId.getValue());
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        if (username == null) return Optional.empty();
        return findByUsername(username.getValue());
    }

    @Override
    public boolean existsByUsername(Username username) {
        if (username == null) return false;
        return existsByUsername(username.getValue());
    }

    @Override
    public void delete(User user) {
        if (user == null || user.getId() == null) return;
        deleteById(user.getId());
    }

    @Override
    public void deleteById(UserID userId) {
        if (userId == null || userId.getValue() == null) return;
        
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            
            UserEntity entity = em.find(UserEntity.class, userId.getValue());
            if (entity != null) {
                em.remove(entity);
                logger.info("Deleted user: {} (ID: {})", entity.getUsername(), userId.getValue());
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error deleting user with ID: {}", userId.getValue(), e);
            throw new RuntimeException("Failed to delete user", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(UserID userId) {
        if (userId == null || userId.getValue() == null) return false;
        
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM UserEntity u WHERE u.id = :id", Long.class);
            query.setParameter("id", userId.getValue());
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking user existence by ID: {}", userId.getValue(), e);
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        return countAll();
    }

    @Override
    public void deleteAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            
            int deletedCount = em.createQuery("DELETE FROM UserEntity").executeUpdate();
            logger.info("Deleted all {} users from database", deletedCount);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error deleting all users", e);
            throw new RuntimeException("Failed to delete all users", e);
        } finally {
            em.close();
        }
    }
}