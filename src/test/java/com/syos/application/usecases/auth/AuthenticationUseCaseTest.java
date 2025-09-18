package com.syos.application.usecases.auth;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.dto.commands.AuthCommand;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.ports.in.AuthenticationPort;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.domain.valueobjects.Password;
import com.syos.domain.valueobjects.Username;
import com.syos.infrastructure.security.BCryptPasswordEncoder;
import com.syos.infrastructure.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationUseCaseTest {

    private InMemoryUserRepository userRepo;
    private AuthenticationPort auth;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryUserRepository();
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        auth = new AuthenticationUseCase(userRepo, encoder);

        // Ensure a known user exists with a known password
        User u = User.createCustomer(Username.of("loginuser"), Email.of("login@user.com"), Password.hash("password123"));
        userRepo.save(u);
    }

    @Test
    void login_success() {
        AuthCommand.LoginCommand cmd = new AuthCommand.LoginCommand("loginuser", "password123");
        AuthResponse res = auth.login(cmd);
        assertTrue(res.isSuccess());
        assertNotNull(res.getSessionId());
        assertEquals("loginuser", res.getUsername());
    }

    @Test
    void login_failureWrongPassword() {
        AuthCommand.LoginCommand cmd = new AuthCommand.LoginCommand("loginuser", "wrong");
        AuthResponse res = auth.login(cmd);
        assertFalse(res.isSuccess());
    }

    @Test
    void registerCustomer_success() {
        AuthCommand.RegisterCommand cmd = new AuthCommand.RegisterCommand("New Name", "newuser1", "new@user1.com", "password123");
        AuthResponse res = auth.registerCustomer(cmd);
        assertTrue(res.isSuccess());
        assertEquals("newuser1", res.getUsername());
    }
}
