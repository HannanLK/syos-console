package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createCustomer_defaultsAndRules() {
        User u = User.createCustomer(Username.of("cus1"), Email.of("c@e.com"), Password.hash("password123"));
        assertEquals(UserRole.CUSTOMER, u.getRole());
        assertTrue(u.isActive());
        assertNotNull(u.getCreatedAt());
        assertEquals(0, u.getSynexPoints().getValue().intValue());
    }

    @Test
    void createEmployee_requiresAdminCreator() {
        assertThrows(NullPointerException.class, () ->
                User.createEmployee(Name.of("Emp"), Username.of("emp1"), Email.of("e@e.com"), Password.hash("password123"), null)
        );
        User emp = User.createEmployee(Name.of("Emp"), Username.of("emp2"), Email.of("e2@e.com"), Password.hash("password123"), UserID.of(1L));
        assertEquals(UserRole.EMPLOYEE, emp.getRole());
        assertTrue(emp.isActive());
    }

    @Test
    void createAdmin_hasAdminRole() {
        User admin = User.createAdmin(Name.of("Admin"), Username.of("adminx"), Email.of("a@e.com"), Password.hash("password123"));
        assertEquals(UserRole.ADMIN, admin.getRole());
    }

    @Test
    void awardSynexPoints_onlyForCustomers() {
        User customer = User.createCustomer(Username.of("c2"), Email.of("c2@e.com"), Password.hash("password123"));
        User updated = customer.awardSynexPoints(Money.of(100.0));
        assertTrue(updated.getSynexPoints().getValue().doubleValue() > 0);

        User employee = User.createEmployee(Name.of("Emp"), Username.of("emp3"), Email.of("e3@e.com"), Password.hash("password123"), UserID.of(1L));
        assertThrows(IllegalStateException.class, () -> employee.awardSynexPoints(Money.of(100.0)));
    }

    @Test
    void changePassword_requiresDifferentPassword() {
        Password p1 = Password.hash("password123");
        User customer = User.createCustomer(Username.of("c3"), Email.of("c3@e.com"), p1);
        Password p2 = Password.hash("password456");
        User updated = customer.changePassword(p2);
        assertEquals(p2, updated.getPassword());
        assertThrows(IllegalArgumentException.class, () -> updated.changePassword(Password.fromHash(updated.getPassword().getHash())));
    }

    @Test
    void updateProfile_changesNameAndEmail() {
        User customer = User.createCustomer(Username.of("c4"), Email.of("c4@e.com"), Password.hash("password123"));
        User upd = customer.updateProfile(Name.of("New Name"), Email.of("new@e.com"));
        assertEquals("New Name", upd.getName().getValue());
        assertEquals("new@e.com", upd.getEmail().getValue());
    }

    @Test
    void deactivate_preventsSystemAdminDeactivation() {
        User admin = User.reconstitute(UserID.of(99L), Username.of("admin"), Password.hash("password123"), UserRole.ADMIN,
                Name.of("System Administrator"), Email.of("admin@e.com"), SynexPoints.zero(), true,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), null);
        assertThrows(IllegalStateException.class, admin::deactivate);

        User otherAdmin = User.createAdmin(Name.of("Admin"), Username.of("anotheradmin"), Email.of("a2@e.com"), Password.hash("password123"));
        User deactivated = otherAdmin.deactivate();
        assertFalse(deactivated.isActive());
    }
}
