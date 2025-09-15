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
        entity.setCreatedAt(user.getCreatedAt().getValue());
        entity.setUpdatedAt(LocalDateTime.now());
        if (user.getCreatedBy() != null) {
            entity.setCreatedBy(user.getCreatedBy().getValue());
        }
        entity.setMemberSince(user.getMemberSince().getValue());
    }

    private User mapToDomain(UserEntity entity) {
        return User.withId(
            UserID.of(entity.getId()),
            Username.of(entity.getUsername()),
            Password.fromHash(entity.getPasswordHash()),
            entity.getRole(),
            Name.of(entity.getName()),
            Email.of(entity.getEmail()),
            SynexPoints.of(entity.getSynexPoints()),
            ActiveStatus.of(entity.isActive()),
            CreatedAt.of(entity.getCreatedAt()),
            UpdatedAt.of(entity.getUpdatedAt()),
            entity.getCreatedBy() != null ? UserID.of(entity.getCreatedBy()) : null,
            MemberSince.of(entity.getMemberSince())
        );
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
}