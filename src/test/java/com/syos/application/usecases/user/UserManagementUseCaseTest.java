package com.syos.application.usecases.user;

import com.syos.application.dto.commands.CreateUserCommand;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.security.PasswordEncoder;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserManagementUseCaseTest {

    UserRepository repo;
    PasswordEncoder encoder;
    UserManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        useCase = new UserManagementUseCase(repo, encoder);
    }

    private static User mkAdmin(long id, boolean active) {
        return User.withId(
                new UserID(id),
                new Username("adminuser"),
                Password.hash("P@ssw0rd!"),
                UserRole.ADMIN,
                Name.of("Admin"),
                Email.of("admin@syos.lk"),
                SynexPoints.zero(),
                ActiveStatus.of(active),
                LocalDateTime.now(),
                UpdatedAt.of(LocalDateTime.now()),
                null,
                MemberSince.of(LocalDateTime.now())
        );
    }

    private static User mkEmployee(long id) {
        return User.withId(
                new UserID(id),
                new Username("emp01"),
                Password.hash("P@ssw0rd!"),
                UserRole.EMPLOYEE,
                Name.of("Emp"),
                Email.of("emp@syos.lk"),
                SynexPoints.zero(),
                ActiveStatus.of(true),
                LocalDateTime.now(),
                UpdatedAt.of(LocalDateTime.now()),
                new UserID(1L),
                MemberSince.of(LocalDateTime.now())
        );
    }

    private static User mkCustomer(long id) {
        return User.withId(
                new UserID(id),
                new Username("cust01"),
                Password.hash("P@ssw0rd!"),
                UserRole.CUSTOMER,
                Name.of("Cust"),
                Email.of("c@syos.lk"),
                SynexPoints.zero(),
                ActiveStatus.of(true),
                LocalDateTime.now(),
                UpdatedAt.of(LocalDateTime.now()),
                null,
                MemberSince.of(LocalDateTime.now())
        );
    }

    @Nested
    class CreateCustomer {
        @Test
        void success_path() {
            CreateUserCommand.CreateCustomerCommand cmd = new CreateUserCommand.CreateCustomerCommand(
                    "john", "john@mail.com", "P@ssw0rd!"
            );
            when(repo.existsByUsername("john")).thenReturn(false);
            when(repo.existsByEmail("john@mail.com")).thenReturn(false);

            // Capture the entity built by the use case and return a copy with ID
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            when(repo.save(captor.capture())).thenAnswer(inv -> {
                User u = captor.getValue();
                return User.withId(new UserID(100L), u.getUsername(), Password.fromHash(u.getPassword().getHash()),
                        u.getRole(), u.getName(), u.getEmail(), u.getSynexPoints(), u.getActiveStatus(),
                        u.getCreatedAt(), UpdatedAt.of(u.getUpdatedAt()), null, u.getMemberSince());
            });

            UserResponse resp = useCase.createCustomer(cmd);
            assertTrue(resp.isSuccess());
            assertEquals(100L, resp.getUserId());
            assertEquals("john", resp.getUsername());
            assertEquals(UserRole.CUSTOMER, resp.getRole());
        }

        @Test
        void username_exists() {
            var cmd = new CreateUserCommand.CreateCustomerCommand("john", "j@mail", "P@ssw0rd!");
            when(repo.existsByUsername("john")).thenReturn(true);
            UserResponse resp = useCase.createCustomer(cmd);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("username"));
        }

        @Test
        void email_exists() {
            var cmd = new CreateUserCommand.CreateCustomerCommand("john", "j@mail.com", "P@ssw0rd!");
            when(repo.existsByUsername("john")).thenReturn(false);
            when(repo.existsByEmail("j@mail.com")).thenReturn(true);
            UserResponse resp = useCase.createCustomer(cmd);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("email"));
        }

        @Test
        void validation_failure_invalid_email() {
            var cmd = new CreateUserCommand.CreateCustomerCommand("john", "bad", "P@ssw0rd!");
            UserResponse resp = useCase.createCustomer(cmd);
            assertFalse(resp.isSuccess());
            // Invalid email triggers domain exception caught by generic handler -> generic failure message
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }

        @Test
        void repository_exception_is_caught() {
            var cmd = new CreateUserCommand.CreateCustomerCommand("john", "j@mail.com", "P@ssw0rd!");
            when(repo.existsByUsername("john")).thenThrow(new RuntimeException("DB down"));
            UserResponse resp = useCase.createCustomer(cmd);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }
    }

    @Nested
    class CreateEmployee {
        @Test
        void success_path_when_admin_ok() {
            var cmd = new CreateUserCommand.CreateEmployeeCommand("Emp Name", "emp", "e@mail.com", "P@ssw0rd!", 1L);
            when(repo.findById(1L)).thenReturn(Optional.of(mkAdmin(1L, true)));
            when(repo.existsByUsername("emp")).thenReturn(false);
            when(repo.existsByEmail("e@mail.com")).thenReturn(false);
            when(repo.save(any())).thenAnswer(inv -> mkEmployee(200L));

            UserResponse resp = useCase.createEmployee(cmd);
            assertTrue(resp.isSuccess());
            assertEquals(200L, resp.getUserId());
            assertEquals(UserRole.EMPLOYEE, resp.getRole());
        }

        @Test
        void fails_when_admin_missing_or_inactive_or_not_admin() {
            var cmd = new CreateUserCommand.CreateEmployeeCommand("Emp", "emp", "e@mail.com", "P@ssw0rd!", 99L);
            when(repo.findById(99L)).thenReturn(Optional.empty());
            UserResponse resp = useCase.createEmployee(cmd);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("admins"));

            // inactive admin
            when(repo.findById(99L)).thenReturn(Optional.of(mkAdmin(99L, false)));
            resp = useCase.createEmployee(cmd);
            assertFalse(resp.isSuccess());

            // non-admin (employee)
            when(repo.findById(99L)).thenReturn(Optional.of(mkEmployee(99L)));
            resp = useCase.createEmployee(cmd);
            assertFalse(resp.isSuccess());
        }

        @Test
        void uniqueness_conflicts_and_validation_and_exception() {
            // validation failure: missing name
            var bad = new CreateUserCommand.CreateEmployeeCommand(" ", "emp", "e@mail.com", "P@ssw0rd!", 1L);
            UserResponse resp = useCase.createEmployee(bad);
            assertFalse(resp.isSuccess());

            var ok = new CreateUserCommand.CreateEmployeeCommand("Emp", "emp", "e@mail.com", "P@ssw0rd!", 1L);
            when(repo.findById(1L)).thenReturn(Optional.of(mkAdmin(1L, true)));
            when(repo.existsByUsername("emp")).thenReturn(true);
            resp = useCase.createEmployee(ok);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("username"));

            when(repo.existsByUsername("emp")).thenReturn(false);
            when(repo.existsByEmail("e@mail.com")).thenReturn(true);
            resp = useCase.createEmployee(ok);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("email"));

            when(repo.existsByEmail("e@mail.com")).thenReturn(false);
            when(repo.save(any())).thenThrow(new RuntimeException("DB issue"));
            resp = useCase.createEmployee(ok);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }
    }

    @Nested
    class CreateAdmin {
        @Test
        void success_and_conflicts_and_validation_and_exception() {
            var ok = new CreateUserCommand.CreateAdminCommand("Boss", "boss", "b@mail.com", "P@ssw0rd!");
            when(repo.existsByUsername("boss")).thenReturn(false);
            when(repo.existsByEmail("b@mail.com")).thenReturn(false);
            when(repo.save(any())).thenAnswer(inv -> mkAdmin(300L, true));
            UserResponse resp = useCase.createAdmin(ok);
            assertTrue(resp.isSuccess());
            assertEquals(300L, resp.getUserId());
            assertEquals(UserRole.ADMIN, resp.getRole());

            // username conflict
            when(repo.existsByUsername("boss")).thenReturn(true);
            resp = useCase.createAdmin(ok);
            assertFalse(resp.isSuccess());

            // email conflict
            when(repo.existsByUsername("boss")).thenReturn(false);
            when(repo.existsByEmail("b@mail.com")).thenReturn(true);
            resp = useCase.createAdmin(ok);
            assertFalse(resp.isSuccess());

            // validation failure (bad email)
            var bad = new CreateUserCommand.CreateAdminCommand("Boss", "boss", "bad", "P@ssw0rd!");
            resp = useCase.createAdmin(bad);
            assertFalse(resp.isSuccess());

            // repository exception
            when(repo.existsByEmail("b@mail.com")).thenThrow(new RuntimeException("down"));
            resp = useCase.createAdmin(ok);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }
    }

    @Nested
    class DeactivateAndProfile {
        @Test
        void deactivate_success_and_not_found_and_illegal_state_and_exception() {
            // success
            when(repo.findById(10L)).thenReturn(Optional.of(mkCustomer(10L)));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UserResponse resp = useCase.deactivateUser(10L);
            assertTrue(resp.isSuccess());
            assertEquals(10L, resp.getUserId());
            assertFalse(resp.isActive());

            // not found
            when(repo.findById(11L)).thenReturn(Optional.empty());
            resp = useCase.deactivateUser(11L);
            assertFalse(resp.isSuccess());

            // illegal state: cannot deactivate system admin (username "admin")
            User systemAdmin = User.withId(
                    new UserID(1L), new Username("admin"), Password.hash("P@ssw0rd!"), UserRole.ADMIN,
                    Name.of("System"), Email.of("admin@syos.lk"), SynexPoints.zero(), ActiveStatus.of(true),
                    LocalDateTime.now(), UpdatedAt.of(LocalDateTime.now()), null, MemberSince.of(LocalDateTime.now())
            );
            when(repo.findById(1L)).thenReturn(Optional.of(systemAdmin));
            UserResponse illegal = useCase.deactivateUser(1L);
            assertFalse(illegal.isSuccess());
            assertTrue(illegal.getMessage().toLowerCase().contains("cannot deactivate"));

            // generic exception
            when(repo.findById(99L)).thenThrow(new RuntimeException("db"));
            resp = useCase.deactivateUser(99L);
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }

        @Test
        void update_profile_success_not_found_validation_exception() {
            User existing = mkCustomer(50L);
            when(repo.findById(50L)).thenReturn(Optional.of(existing));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserResponse resp = useCase.updateProfile(50L, "New Name", "new@mail.com");
            assertTrue(resp.isSuccess());
            assertEquals("New Name", resp.getName());
            assertEquals("new@mail.com", resp.getEmail());

            // not found
            when(repo.findById(51L)).thenReturn(Optional.empty());
            resp = useCase.updateProfile(51L, "Name", "mail@mail.com");
            assertFalse(resp.isSuccess());

            // validation failure
            resp = useCase.updateProfile(50L, "N", "bad");
            assertFalse(resp.isSuccess());

            // exception
            when(repo.findById(52L)).thenThrow(new RuntimeException("db"));
            resp = useCase.updateProfile(52L, "Name Two", "n2@mail.com");
            assertFalse(resp.isSuccess());
            assertTrue(resp.getMessage().toLowerCase().contains("failed"));
        }
    }

    @Test
    @DisplayName("canCreateEmployee: covers admin missing, inactive, non-admin and active admin")
    void canCreateEmployee_allPaths() {
        // missing
        when(repo.findById(1L)).thenReturn(Optional.empty());
        var result1 = invokeCanCreateEmployee(1L);
        assertFalse(result1);

        // inactive admin
        when(repo.findById(2L)).thenReturn(Optional.of(mkAdmin(2L, false)));
        var result2 = invokeCanCreateEmployee(2L);
        assertFalse(result2);

        // non-admin
        when(repo.findById(3L)).thenReturn(Optional.of(mkEmployee(3L)));
        var result3 = invokeCanCreateEmployee(3L);
        assertFalse(result3);

        // active admin
        when(repo.findById(4L)).thenReturn(Optional.of(mkAdmin(4L, true)));
        var result4 = invokeCanCreateEmployee(4L);
        assertTrue(result4);
    }

    // Use reflection to call private canCreateEmployee for coverage (service-level behavior implied)
    private boolean invokeCanCreateEmployee(Long id) {
        try {
            var m = UserManagementUseCase.class.getDeclaredMethod("canCreateEmployee", Long.class);
            m.setAccessible(true);
            return (boolean) m.invoke(useCase, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
