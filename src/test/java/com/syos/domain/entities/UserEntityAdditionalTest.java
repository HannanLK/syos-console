package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityAdditionalTest {

    private static User mk(UserRole role, boolean active, String username) {
        return User.withId(
                new UserID(1L),
                new Username(username),
                Password.hash("Password1"),
                role,
                Name.of("Name"),
                Email.of("mail@syos.lk"),
                SynexPoints.zero(),
                ActiveStatus.of(active),
                LocalDateTime.now(),
                UpdatedAt.of(LocalDateTime.now()),
                null,
                MemberSince.of(LocalDateTime.now())
        );
    }

    @Test
    void permissions_authenticate_points_and_password_changes() {
        User customer = mk(UserRole.CUSTOMER, true, "cust1");
        assertTrue(customer.canAccumulatePoints());
        assertFalse(customer.canManageUsers());
        assertFalse(customer.isAdmin());

        // authenticate raw and via Password
        assertTrue(customer.authenticate("Password1"));
        // too-short raw passwords throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> customer.authenticate("wrong"));
        // valid-length wrong password returns false
        assertFalse(customer.authenticate("Password2"));
        assertTrue(customer.authenticate(Password.hash("Password1")));

        // add and redeem points success
        customer.addSynexPoints(SynexPoints.of(new java.math.BigDecimal("5.00")));
        assertTrue(customer.getSynexPoints().getValue().compareTo(java.math.BigDecimal.ZERO) > 0);
        customer.redeemSynexPoints(SynexPoints.of(new java.math.BigDecimal("1.00")));
        assertTrue(customer.getSynexPoints().getValue().compareTo(java.math.BigDecimal.ZERO) >= 0);

        // award points success
        User awarded = customer.awardSynexPoints(Money.of(new java.math.BigDecimal("1000.00")));
        assertTrue(awarded.getSynexPoints().getValue().compareTo(customer.getSynexPoints().getValue()) > 0);

        // cannot award points when not allowed
        User employee = mk(UserRole.EMPLOYEE, true, "emp1");
        assertThrows(IllegalStateException.class, () -> employee.awardSynexPoints(Money.of(100)));

        // deactivate/reactivate and system admin protection
        User admin = mk(UserRole.ADMIN, true, "admin");
        assertThrows(IllegalStateException.class, admin::deactivate);
        User admin2 = mk(UserRole.ADMIN, true, "boss").deactivate();
        assertFalse(admin2.isActive());
        assertTrue(admin2.reactivate().isActive());

        // change password - two variants
        User changed = mk(UserRole.CUSTOMER, true, "cust2").changePassword(Password.hash("NewPassword1"));
        assertNotEquals(changed.getPassword().getHash(), customer.getPassword().getHash());

        // change with old password verification
        User toChange = mk(UserRole.CUSTOMER, true, "cust3");
        assertThrows(com.syos.domain.exceptions.AuthenticationException.class,
                () -> toChange.changePassword(Password.hash("WrongOld"), Password.hash("NewerPass1")));
        toChange.changePassword(Password.hash("Password1"), Password.hash("AnotherPass1"));
        assertTrue(toChange.authenticate("AnotherPass1"));
    }
}
