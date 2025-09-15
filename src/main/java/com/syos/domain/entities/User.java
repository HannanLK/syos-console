package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private final UserID id;
    private final Username username;
    private final Password password; // stores hash
    private final UserRole role;
    private final Name name;
    private final Email email;
    private final SynexPoints synexPoints;
    private final ActiveStatus activeStatus;
    private final CreatedAt createdAt;
    private final UpdatedAt updatedAt;
    private final UserID createdBy; // optional
    private final MemberSince memberSince;

    // Private constructor for internal use
    private User(UserID id,
                 Username username,
                 Password password,
                 UserRole role,
                 Name name,
                 Email email,
                 SynexPoints synexPoints,
                 ActiveStatus activeStatus,
                 CreatedAt createdAt,
                 UpdatedAt updatedAt,
                 UserID createdBy,
                 MemberSince memberSince) {
        this.id = id;
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.role = Objects.requireNonNull(role);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.synexPoints = synexPoints == null ? SynexPoints.zero() : synexPoints;
        this.activeStatus = activeStatus == null ? ActiveStatus.active() : activeStatus;
        this.createdAt = createdAt == null ? CreatedAt.now() : createdAt;
        this.updatedAt = updatedAt == null ? UpdatedAt.now() : updatedAt;
        this.createdBy = createdBy; // may be null
        this.memberSince = memberSince == null ? MemberSince.fromCreatedAt(this.createdAt) : memberSince;
    }

    public static User registerNew(Username username,
                                   String rawPassword,
                                   Name name,
                                   Email email) {
        return createWithRole(username, rawPassword, name, email, UserRole.CUSTOMER, null);
    }

    /**
     * Generic factory to create a user with the specified role.
     */
    public static User createWithRole(Username username,
                                      String rawPassword,
                                      Name name,
                                      Email email,
                                      UserRole role,
                                      UserID createdBy) {
        Password hashed = Password.hash(rawPassword);
        LocalDateTime now = LocalDateTime.now();
        return new User(null,
                username,
                hashed,
                role,
                name,
                email,
                SynexPoints.zero(),
                ActiveStatus.active(),
                CreatedAt.of(now),
                UpdatedAt.of(now),
                createdBy,
                null);
    }

    /**
     * Factory method to create a User with an ID (for repository use)
     */
    public static User withId(UserID id,
                             Username username,
                             Password password,
                             UserRole role,
                             Name name,
                             Email email,
                             SynexPoints synexPoints,
                             ActiveStatus activeStatus,
                             CreatedAt createdAt,
                             UpdatedAt updatedAt,
                             UserID createdBy,
                             MemberSince memberSince) {
        return new User(id, username, password, role, name, email, 
                       synexPoints, activeStatus, createdAt, updatedAt, 
                       createdBy, memberSince);
    }

    /**
     * Create a new User instance with the provided ID (used when loading from storage)
     */
    public User withId(UserID newId) {
        return new User(newId, username, password, role, name, email,
                       synexPoints, activeStatus, createdAt, updatedAt,
                       createdBy, memberSince);
    }

    public UserID getId() { return id; }
    public Username getUsername() { return username; }
    public Password getPassword() { return password; }
    public UserRole getRole() { return role; }
    public Name getName() { return name; }
    public Email getEmail() { return email; }
    public SynexPoints getSynexPoints() { return synexPoints; }
    public boolean isActive() { return activeStatus.isActive(); }
    public ActiveStatus getActiveStatus() { return activeStatus; }
    public CreatedAt getCreatedAt() { return createdAt; }
    public UpdatedAt getUpdatedAt() { return updatedAt; }
    public UserID getCreatedBy() { return createdBy; }
    public MemberSince getMemberSince() { return memberSince; }
}