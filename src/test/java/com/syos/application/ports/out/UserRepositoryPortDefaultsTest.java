package com.syos.application.ports.out;

import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryPortDefaultsTest {

    static class FakeRepo implements UserRepository {
        final Map<Long, User> store = new HashMap<>();
        long seq = 1;

        @Override
        public Optional<User> findById(UserID userId) { return Optional.ofNullable(store.get(userId.getValue())); }

        @Override
        public Optional<User> findByUsername(Username username) {
            return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }

        @Override
        public boolean existsByUsername(Username username) { return findByUsername(username).isPresent(); }

        @Override
        public boolean existsByEmail(String email) { return store.values().stream().anyMatch(u -> u.getEmail().getValue().equalsIgnoreCase(email)); }

        @Override
        public User save(User user) {
            Long id = user.getId() != null ? user.getId().getValue() : seq++;
            User saved = User.withId(new UserID(id), user.getUsername(), user.getPassword(), user.getRole(),
                    user.getName(), user.getEmail(), user.getSynexPoints(), user.getActiveStatus(),
                    user.getCreatedAt(), UpdatedAt.of(user.getUpdatedAt()), null, user.getMemberSince());
            store.put(id, saved);
            return saved;
        }

        @Override
        public void delete(User user) { if (user.getId() != null) store.remove(user.getId().getValue()); }

        @Override
        public void deleteById(UserID userId) { store.remove(userId.getValue()); }

        @Override
        public List<User> findAll() { return new ArrayList<>(store.values()); }

        @Override
        public boolean existsById(UserID userId) { return store.containsKey(userId.getValue()); }

        @Override
        public long count() { return store.size(); }

        @Override
        public void deleteAll() { store.clear(); }
    }

    FakeRepo repo;

    @BeforeEach
    void setup() {
        repo = new FakeRepo();
        // seed some users
        repo.save(User.withId(null,
                new Username("alice"), Password.hash("Password1"), UserRole.CUSTOMER,
                Name.of("Alice"), Email.of("alice@mail.com"), SynexPoints.zero(), ActiveStatus.of(true),
                LocalDateTime.now(), UpdatedAt.of(LocalDateTime.now()), null, MemberSince.of(LocalDateTime.now())));
        repo.save(User.withId(null,
                new Username("bob"), Password.hash("Password1"), UserRole.EMPLOYEE,
                Name.of("Bob"), Email.of("bob@mail.com"), SynexPoints.zero(), ActiveStatus.of(true),
                LocalDateTime.now(), UpdatedAt.of(LocalDateTime.now()), null, MemberSince.of(LocalDateTime.now())));
        repo.save(User.withId(null,
                new Username("carol"), Password.hash("Password1"), UserRole.ADMIN,
                Name.of("Carol"), Email.of("carol@mail.com"), SynexPoints.zero(), ActiveStatus.of(true),
                LocalDateTime.now(), UpdatedAt.of(LocalDateTime.now()), null, MemberSince.of(LocalDateTime.now())));
    }

    @Test
    void stringOverloads_delegateToValueObjectVariants() {
        assertTrue(repo.findByUsername("alice").isPresent());
        assertTrue(repo.existsByUsername("bob"));
        assertTrue(repo.findById(1L).isPresent());
        assertFalse(repo.existsByUsername("unknown"));
    }

    @Test
    void count_helpers_and_searchUsers_coverDefaults() {
        assertEquals(3, repo.countAll());
        assertEquals(1, repo.countByRole(UserRole.ADMIN));
        assertEquals(1, repo.countByRole(UserRole.EMPLOYEE));
        assertEquals(1, repo.countByRole(UserRole.CUSTOMER));

        var results = repo.searchUsers("al"); // matches alice name and username
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().getValue().equals("alice")));

        // case-insensitive search by email
        var results2 = repo.searchUsers("MAIL.COM");
        assertEquals(3, results2.size());
    }
}
