package com.syos.application.ports.in;

/**
 * Application-facing API for authentication and user provisioning use cases.
 * Adapters (CLI/REST) should depend on this interface only.
 */
public interface AuthenticationPort {
    boolean login(String username, String password);
    void logout(String username);

    void registerCustomer(String username, String password, String name, String email);

    void createAdmin(String username, String password, String name, String email);

    void createEmployee(String username, String password, String name, String email);
}
