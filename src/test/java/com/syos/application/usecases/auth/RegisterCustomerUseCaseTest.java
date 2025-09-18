package com.syos.application.usecases.auth;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterCustomerUseCaseTest {

    private InMemoryUserRepository userRepo;
    private RegisterCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryUserRepository();
        useCase = new RegisterCustomerUseCase(userRepo);
    }

    @Test
    void register_successfullyCreatesCustomer() {
        User u = useCase.register("newuser", "password123", "New User", "new@user.com");
        assertNotNull(u);
        assertEquals("newuser", u.getUsername().getValue());
        assertEquals("New User", u.getName().getValue());
        assertEquals("new@user.com", u.getEmail().getValue());
    }

    @Test
    void register_duplicateUsernameFails() {
        // default repo has 'customer' and 'admin', but we add one explicitly
        useCase.register("uniqueuser", "password123", "A", "a@a.com");
        assertThrows(RuntimeException.class, () -> useCase.register("uniqueuser", "password123", "B", "b@b.com"));
    }

    @Test
    void register_rejectsWeakPassword() {
        Exception ex = assertThrows(RuntimeException.class, () -> useCase.register("x", "short", "n", "e@e.com"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }
}
