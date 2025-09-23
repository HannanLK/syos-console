package com.syos.application.usecases.auth;

import com.syos.domain.exceptions.InvalidUsernameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogoutUseCaseTest {

    @Test
    void logout_withValidUsername_shouldDoNothing() {
        LogoutUseCase useCase = new LogoutUseCase();
        assertDoesNotThrow(() -> useCase.logout("valid_user1"));
    }

    @Test
    void logout_withInvalidUsername_shouldPropagateValidationError() {
        LogoutUseCase useCase = new LogoutUseCase();
        assertThrows(InvalidUsernameException.class, () -> useCase.logout(" "));
        assertThrows(InvalidUsernameException.class, () -> useCase.logout("Bad Spaces"));
        assertThrows(InvalidUsernameException.class, () -> useCase.logout("@bad"));
    }
}
