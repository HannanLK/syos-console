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
        Password hashed = Password.hash(rawPassword);
        LocalDateTime now = LocalDateTime.now();
        return new User(null,
                username,
                hashed,
                UserRole.CUSTOMER,
                name,
                email,
                SynexPoints.zero(),
                ActiveStatus.active(),
                CreatedAt.of(now),
                UpdatedAt.of(now),
                null,
                null);
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
