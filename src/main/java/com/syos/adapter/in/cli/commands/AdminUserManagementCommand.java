package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.domain.valueobjects.UserID;
import com.syos.domain.valueobjects.Username;
import com.syos.shared.enums.UserRole;

import java.util.List;

/**
 * Simple Admin User Management command:
 * - List users
 * - Create customer/employee
 * - Deactivate/Activate user
 */
public class AdminUserManagementCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    public AdminUserManagementCommand(ConsoleIO console, SessionManager sessionManager, UserRepository userRepository) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            console.printError("Admin access required.");
            return;
        }

        while (true) {
            console.println("\n=== USER MANAGEMENT ===");
            console.println("1. View All Users");
            console.println("2. Create Customer");
            console.println("3. Create Employee");
            console.println("4. Edit User");
            console.println("5. Delete User");
            console.println("6. Deactivate User");
            console.println("7. Activate User");
            console.println("8. Back");
            String choice = console.readLine("Choose: ");
            switch (choice) {
                case "1" -> listUsers();
                case "2" -> createUser(UserRole.CUSTOMER);
                case "3" -> createUser(UserRole.EMPLOYEE);
                case "4" -> editUser();
                case "5" -> deleteUser();
                case "6" -> toggleActive(false);
                case "7" -> toggleActive(true);
                case "8" -> { return; }
                default -> console.printError("Invalid choice");
            }
        }
    }

    private void listUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            console.println("No users found.");
            return;
        }
        console.println(String.format("%-5s %-15s %-20s %-25s %-10s %-8s", "ID", "Username", "Name", "Email", "Role", "Active"));
        for (User u : users) {
            console.println(String.format("%-5d %-15s %-20s %-25s %-10s %-8s",
                    u.getId() != null ? u.getId().getValue() : 0,
                    u.getUsername().getValue(),
                    u.getName().getValue(),
                    u.getEmail().getValue(),
                    u.getRole().name(),
                    u.isActive() ? "YES" : "NO"));
        }
    }

    private void createUser(UserRole role) {
        String name = console.readLine("Name: ");
        String username = console.readLine("Username: ");
        String email = console.readLine("Email: ");
        String password = console.readPassword("Password: ");
        try {
            UserID createdBy = UserID.of(sessionManager.getCurrentUserId());
            User user = User.createWithRole(Username.of(username), password, Name.of(name), Email.of(email), role, createdBy);
            userRepository.save(user);
            console.printSuccess(role + " created: " + username);
        } catch (Exception e) {
            console.printError("Failed to create user: " + e.getMessage());
        }
    }

    private void toggleActive(boolean activate) {
        String idStr = console.readLine("User ID: ");
        try {
            long id = Long.parseLong(idStr);
            userRepository.findById(new UserID(id)).ifPresentOrElse(user -> {
                User updated;
                if (activate && !user.isActive()) {
                    // domain has reactivate()
                    user.reactivate();
                    updated = user;
                } else if (!activate && user.isActive()) {
                    user.deactivate();
                    updated = user;
                } else {
                    console.printWarning("No status change required.");
                    return;
                }
                userRepository.save(updated);
                console.printSuccess("User status updated.");
            }, () -> console.printError("User not found."));
        } catch (NumberFormatException ex) {
            console.printError("Invalid ID");
        }
    }

    private void editUser() {
        String idStr = console.readLine("User ID to edit: ");
        try {
            long id = Long.parseLong(idStr);
            userRepository.findById(new UserID(id)).ifPresentOrElse(user -> {
                String newName = console.readLine("New Name (blank to keep '" + user.getName().getValue() + "'): ");
                String newEmail = console.readLine("New Email (blank to keep '" + user.getEmail().getValue() + "'): ");
                String roleStr = console.readLine("New Role [CUSTOMER/EMPLOYEE/ADMIN] (blank to keep '" + user.getRole().name() + "'): ");
                try {
                    // Update name/email via domain method
                    Name updatedName = (newName != null && !newName.isBlank()) ? Name.of(newName) : user.getName();
                    Email updatedEmail = (newEmail != null && !newEmail.isBlank()) ? Email.of(newEmail) : user.getEmail();
                    user.updateProfile(updatedName, updatedEmail);

                    if (roleStr != null && !roleStr.isBlank()) {
                        console.printWarning("Role changes are not supported via this screen (domain role is immutable). Skipping role update.");
                    }

                    userRepository.save(user);
                    console.printSuccess("User updated successfully.");
                } catch (Exception ex) {
                    console.printError("Failed to update user: " + ex.getMessage());
                }
            }, () -> console.printError("User not found."));
        } catch (NumberFormatException ex) {
            console.printError("Invalid ID");
        }
    }

    private void deleteUser() {
        String idStr = console.readLine("User ID to delete: ");
        try {
            long id = Long.parseLong(idStr);
            userRepository.findById(new UserID(id)).ifPresentOrElse(user -> {
                String confirm = console.readLine("Are you sure you want to delete '" + user.getUsername().getValue() + "'? (y/N): ");
                if ("y".equalsIgnoreCase(confirm)) {
                    try {
                        userRepository.deleteById(new UserID(id));
                        console.printSuccess("User deleted.");
                    } catch (Exception ex) {
                        console.printError("Delete failed: " + ex.getMessage());
                    }
                } else {
                    console.println("Cancelled.");
                }
            }, () -> console.printError("User not found."));
        } catch (NumberFormatException ex) {
            console.printError("Invalid ID");
        }
    }
}
