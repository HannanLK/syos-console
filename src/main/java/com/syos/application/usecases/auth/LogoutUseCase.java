package com.syos.application.usecases.auth;

import com.syos.domain.valueobjects.Username;

/**
 * Use case: logout a user. In a console app without session store, this is a no-op
 * provided for architectural completeness.
 */
public class LogoutUseCase {
    public void logout(String username) {
        // Validate a username format to surface domain errors consistently
        Username.of(username);
        // No session management implemented; placeholder
    }
}
