package com.syos.application.usecases.auth;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.domain.valueobjects.Password;
import com.syos.domain.valueobjects.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateAdminUseCaseTest {

    private InMemoryUserRepository userRepo;
    private CreateAdminUseCase useCase;

    @BeforeEach
    void setup() {
        userRepo = new InMemoryUserRepository();
        useCase = new CreateAdminUseCase(userRepo);
        // Seed existing data
        User existing = User.createAdmin(
                Name.of("Admin One"),
                Username.of("admin1"),
                Email.of("admin1@syos.lk"),
                Password.hash("secret123"));
        userRepo.save(existing);
    }

    @Test
    void createAdmin_success() {
        User u = useCase.create("newadmin", "password123", "New Admin", "newadmin@syos.lk");
        assertNotNull(u.getId());
        assertEquals("newadmin", u.getUsername().getValue());
    }

    @Test
    void createAdmin_duplicateUsername_throws() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                useCase.create("admin1", "password123", "Someone", "someone@syos.lk")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void createAdmin_duplicateEmail_throws() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                useCase.create("uniqueuser", "password123", "Someone", "admin1@syos.lk")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }
}
