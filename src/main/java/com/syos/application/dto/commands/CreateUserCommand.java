package com.syos.application.dto.commands;

/**
 * User Creation Commands
 *
 * Immutable DTOs carrying data for user creation use cases.
 */
public final class CreateUserCommand {

    private CreateUserCommand() { /* no instances */ }

    /**
     * Command for creating customer accounts
     */
    public static final class CreateCustomerCommand {
        private final String username;
        private final String email;
        private final String password;

        public CreateCustomerCommand(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    /**
     * Command for creating employee accounts (must be initiated by an admin)
     */
    public static final class CreateEmployeeCommand {
        private final String name;
        private final String username;
        private final String email;
        private final String password;
        private final Long createdBy; // Admin user ID

        public CreateEmployeeCommand(String name, String username, String email, String password, Long createdBy) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.password = password;
            this.createdBy = createdBy;
        }

        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Long getCreatedBy() { return createdBy; }
    }

    /**
     * Command for creating administrator accounts
     */
    public static final class CreateAdminCommand {
        private final String name;
        private final String username;
        private final String email;
        private final String password;

        public CreateAdminCommand(String name, String username, String email, String password) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
}
