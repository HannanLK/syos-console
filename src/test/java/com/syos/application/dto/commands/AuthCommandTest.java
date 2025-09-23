package com.syos.application.dto.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthCommandTest {

    @Test
    void loginCommand_shouldExposeFieldsAndToString() {
        AuthCommand.LoginCommand cmd = new AuthCommand.LoginCommand("john", "secret");
        assertEquals("john", cmd.getUsername());
        assertEquals("secret", cmd.getPassword());
        assertTrue(cmd.toString().contains("john"));
    }

    @Test
    void registerCommand_shouldExposeFieldsAndToString() {
        AuthCommand.RegisterCommand cmd = new AuthCommand.RegisterCommand("John Doe", "john", "john@mail", "secret");
        assertEquals("john", cmd.getUsername());
        assertEquals("john@mail", cmd.getEmail());
        assertEquals("secret", cmd.getPassword());
        assertEquals("John Doe", cmd.getName());
        String s = cmd.toString();
        assertTrue(s.contains("john"));
        assertTrue(s.contains("john@mail"));
        assertTrue(s.contains("John Doe"));
    }
}
