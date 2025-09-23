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

class CreateEmployeeUseCaseTest {

    private InMemoryUserRepository userRepo;
    private CreateEmployeeUseCase useCase;

    @BeforeEach
    void setup() {
        userRepo = new InMemoryUserRepository();
        useCase = new CreateEmployeeUseCase(userRepo);
        // Seed existing user to test duplicates
        User existing = User.createEmployee(
                Name.of("Emp One"),
                Username.of("emp1"),
                Email.of("emp1@syos.lk"),
                Password.hash("secret123"),
                com.syos.domain.valueobjects.UserID.of(1L)
        );
        userRepo.save(existing);
    }

    @Test
    void createEmployee_success() {
        CreateEmployeeUseCase.CreateEmployeeRequest req = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name("Jane Doe")
                .username("jane")
                .email("jane@syos.lk")
                .password("secret1")
                .createdBy(1L);

        CreateEmployeeUseCase.CreateEmployeeResponse res = useCase.execute(req);
        assertTrue(res.isSuccess());
        assertNotNull(res.getUserId());
    }

    @Test
    void createEmployee_duplicateUsername() {
        CreateEmployeeUseCase.CreateEmployeeRequest req = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name("Dup User")
                .username("emp1") // already exists
                .email("dup@syos.lk")
                .password("secret1")
                .createdBy(1L);

        CreateEmployeeUseCase.CreateEmployeeResponse res = useCase.execute(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void createEmployee_duplicateEmail() {
        // Seed email duplicate with another username
        User existing2 = User.createEmployee(
                Name.of("Emp Two"),
                Username.of("emp2"),
                Email.of("dupemail@syos.lk"),
                Password.hash("secret123"),
                com.syos.domain.valueobjects.UserID.of(1L)
        );
        userRepo.save(existing2);

        CreateEmployeeUseCase.CreateEmployeeRequest req = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name("Dup Email")
                .username("uniqueuser")
                .email("dupemail@syos.lk") // duplicate
                .password("secret1")
                .createdBy(1L);

        CreateEmployeeUseCase.CreateEmployeeResponse res = useCase.execute(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void createEmployee_validationFailures() {
        // Null request
        assertFalse(useCase.execute(null).isSuccess());

        // Missing fields
        CreateEmployeeUseCase.CreateEmployeeRequest req = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name(" ")
                .username(" ")
                .email(" ")
                .password(" ")
                .createdBy(0L);
        var res = useCase.execute(req);
        assertFalse(res.isSuccess());
        assertNotNull(res.getMessage());

        // Too short password
        var req2 = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name("Ok")
                .username("okuser")
                .email("ok@syos.lk")
                .password("123")
                .createdBy(1L);
        var res2 = useCase.execute(req2);
        assertFalse(res2.isSuccess());
        assertTrue(res2.getMessage().contains("6"));
    }
}
