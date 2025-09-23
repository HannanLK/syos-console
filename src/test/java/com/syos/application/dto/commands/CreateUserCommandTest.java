package com.syos.application.dto.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserCommandTest {

    @Test
    void createCustomerCommand_exposesFields() {
        CreateUserCommand.CreateCustomerCommand c = new CreateUserCommand.CreateCustomerCommand("john","j@mail.com","Secret123");
        assertEquals("john", c.getUsername());
        assertEquals("j@mail.com", c.getEmail());
        assertEquals("Secret123", c.getPassword());
    }

    @Test
    void createEmployeeCommand_exposesFields() {
        CreateUserCommand.CreateEmployeeCommand c = new CreateUserCommand.CreateEmployeeCommand(
                "Emp Name","emp","e@mail.com","Secret123", 99L);
        assertEquals("Emp Name", c.getName());
        assertEquals("emp", c.getUsername());
        assertEquals("e@mail.com", c.getEmail());
        assertEquals("Secret123", c.getPassword());
        assertEquals(99L, c.getCreatedBy());
    }

    @Test
    void createAdminCommand_exposesFields() {
        CreateUserCommand.CreateAdminCommand c = new CreateUserCommand.CreateAdminCommand(
                "Boss","boss","b@mail.com","Secret123");
        assertEquals("Boss", c.getName());
        assertEquals("boss", c.getUsername());
        assertEquals("b@mail.com", c.getEmail());
        assertEquals("Secret123", c.getPassword());
    }
}
