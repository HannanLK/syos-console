package com.syos.application.usecases.auth;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.domain.valueobjects.Username;
import com.syos.domain.valueobjects.Password;

import java.util.Objects;

/**
 * Use case: provision an ADMIN user.
 */
public class CreateAdminUseCase {
    private final UserRepository userRepository;

    public CreateAdminUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User create(String username, String rawPassword, String name, String email) {
        Username u = Username.of(username);
        Name n = Name.of(name);
        Email e = Email.of(email);

        if (userRepository.existsByUsername(u.getValue())) {
            throw new IllegalStateException("Username already taken");
        }
        if (userRepository.existsByEmail(e.getValue())) {
            throw new IllegalStateException("Email already registered");
        }

        User user = User.createAdmin(n, u, e, Password.hash(rawPassword));
        return userRepository.save(user);
    }
}
