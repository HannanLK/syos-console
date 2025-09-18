package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.auth.CreateEmployeeUseCase;
import com.syos.application.ports.out.*;
import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;
import com.syos.shared.enums.UnitOfMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Admin Controller for SYOS System
 * 
 * Handles all administrative functions including:
 * - User Management 
 * - System Configuration
 * - Advanced Reports
 * - All Employee Functions (including Add Product)
 * 
 * Note: SYNEX Points are NOT included as this is customer-specific functionality
 */
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final AddProductUseCase addProductUseCase;
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeController employeeController;

    public AdminController(ConsoleIO console, 
                          SessionManager sessionManager,
                          AddProductUseCase addProductUseCase,
                          CreateEmployeeUseCase createEmployeeUseCase,
                          UserRepository userRepository,
                          BrandRepository brandRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          EmployeeController employeeController) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.addProductUseCase = addProductUseCase;
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.userRepository = userRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.employeeController = employeeController;
    }

    /**
     * Display the main admin dashboard
     * Note: No SYNEX Points functionality as this is customer-specific
     */
    public void displayAdminDashboard() {
        UserSession currentSession = sessionManager.getCurrentSession();
        User currentUser = getCurrentUser(currentSession);
        
        logger.info("Admin {} accessing dashboard", currentUser.getUsername().getValue());
        
        console.println("\n" + "=".repeat(50));
        console.printSuccess("🛡️  ADMIN DASHBOARD");
        console.println("=".repeat(50));
        console.println("Welcome, Administrator " + currentUser.getName().getValue() + "!");
        console.println();
        
        // Display system overview
        displaySystemOverview();
        
        // Display menu options (NO SYNEX Points)
        console.println("📋 Available Functions:");
        List<String> menuOptions = Arrays.asList(
            "1. User Management",
            "2. Advanced Reports & Analytics", 
            "3. System Configuration",
            "4. All Employee Functions",
            "5. Data Management",
            "6. Security & Audit",
            "7. Account Settings",
            "8. Logout"
            // NOTE: SYNEX Points options REMOVED - customer-specific functionality
        );
        
        menuOptions.forEach(console::println);
        console.println("=".repeat(50));
    }

    /**
     * Handle admin menu selection
     */
    public void handleMenuSelection(int choice) {
        logger.info("Admin menu selection: {}", choice);
        
        switch (choice) {
            case 1: handleUserManagement(); break;
            case 2: handleAdvancedReports(); break;
            case 3: handleSystemConfiguration(); break;
            case 4: handleEmployeeFunctions(); break;
            case 5: handleDataManagement(); break;
            case 6: handleSecurityAudit(); break;
            case 7: handleAccountSettings(); break;
            case 8: handleLogout(); break;
            default: 
                console.printError("❌ Invalid selection. Please choose 1-8.");
                break;
        }
    }

    private void displaySystemOverview() {
        try {
            // Get basic system stats
            long totalUsers = userRepository.countAll();
            long totalEmployees = userRepository.countByRole(UserRole.EMPLOYEE);
            long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
            
            console.println("📊 System Overview:");
            console.println("• Total Users: " + totalUsers);
            console.println("• Employees: " + totalEmployees);
            console.println("• Customers: " + totalCustomers);
            console.println("• System Status: ✅ ACTIVE");
            console.println();
        } catch (Exception e) {
            logger.warn("Error displaying system overview", e);
            console.printWarning("System overview temporarily unavailable");
        }
    }

    private void handleUserManagement() {
        console.println("\n🔧 USER MANAGEMENT");
        console.println("1. Create New Employee");
        console.println("2. View All Users");
        console.println("3. Search Users");
        console.println("4. Update User Information");
        console.println("5. Deactivate/Activate Users");
        console.println("6. Reset User Passwords");
        console.println("7. Back to Admin Dashboard");
        
        int choice = console.readInt("Select option: ");
        
        switch (choice) {
            case 1: handleCreateEmployee(); break;
            case 2: handleViewAllUsers(); break;
            case 3: handleSearchUsers(); break;
            case 4: console.printInfo("Feature coming soon..."); break;
            case 5: console.printInfo("Feature coming soon..."); break;
            case 6: console.printInfo("Feature coming soon..."); break;
            case 7: return;
            default: console.printError("Invalid selection");
        }
    }

    private void handleCreateEmployee() {
        console.println("\n👤 CREATE NEW EMPLOYEE");
        
        try {
            String name = console.readString("Enter employee name: ");
            String username = console.readString("Enter username: ");
            String email = console.readString("Enter email: ");
            String password = console.readString("Enter password: ");
            
            CreateEmployeeUseCase.CreateEmployeeRequest request = new CreateEmployeeUseCase.CreateEmployeeRequest()
                .name(name)
                .username(username)
                .email(email)
                .password(password)
                .createdBy(sessionManager.getCurrentSession().getUserId());
            
            CreateEmployeeUseCase.CreateEmployeeResponse response = createEmployeeUseCase.execute(request);
            
            if (response.isSuccess()) {
                console.printSuccess("✅ Employee created successfully!");
                console.println("Employee ID: " + response.getUserId());
                logger.info("Admin created new employee: {} (ID: {})", username, response.getUserId());
            } else {
                console.printError("❌ Failed to create employee: " + response.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error creating employee", e);
            console.printError("❌ Error creating employee: " + e.getMessage());
        }
    }

    private void handleViewAllUsers() {
        console.println("\n👥 ALL USERS");
        try {
            List<User> users = userRepository.findAll();
            
            if (users.isEmpty()) {
                console.printWarning("No users found in the system.");
                return;
            }
            
            console.println(String.format("%-5s %-20s %-15s %-25s %-10s", 
                "ID", "Name", "Username", "Email", "Role"));
            console.println("-".repeat(80));
            
            for (User user : users) {
                console.println(String.format("%-5d %-20s %-15s %-25s %-10s",
                    user.getId(),
                    user.getName().getValue(),
                    user.getUsername().getValue(),
                    user.getEmail().getValue(),
                    user.getRole().toString()));
            }
            
        } catch (Exception e) {
            logger.error("Error viewing all users", e);
            console.printError("❌ Error retrieving users: " + e.getMessage());
        }
    }

    private void handleSearchUsers() {
        console.println("\n🔍 SEARCH USERS");
        String searchTerm = console.readString("Enter search term (name, username, or email): ");
        
        try {
            List<User> results = userRepository.searchUsers(searchTerm);
            
            if (results.isEmpty()) {
                console.printWarning("No users found matching '" + searchTerm + "'");
                return;
            }
            
            console.println("Search Results:");
            console.println(String.format("%-5s %-20s %-15s %-10s", "ID", "Name", "Username", "Role"));
            console.println("-".repeat(55));
            
            for (User user : results) {
                console.println(String.format("%-5d %-20s %-15s %-10s",
                    user.getId(),
                    user.getName().getValue(),
                    user.getUsername().getValue(),
                    user.getRole().toString()));
            }
            
        } catch (Exception e) {
            logger.error("Error searching users", e);
            console.printError("❌ Error searching users: " + e.getMessage());
        }
    }

    private void handleAdvancedReports() {
        console.printInfo("📊 Advanced Reports & Analytics - Feature coming soon...");
    }

    private void handleSystemConfiguration() {
        console.printInfo("⚙️ System Configuration - Feature coming soon...");
    }

    /**
     * Handle all employee functions - Admin has access to all employee capabilities
     * This includes the Add Product feature
     */
    private void handleEmployeeFunctions() {
        console.println("\n👨‍💼 EMPLOYEE FUNCTIONS (Admin Access)");
        console.println("You have access to all employee functions:");
        console.println();
        
        // Delegate to employee controller
        employeeController.displayEmployeeDashboard();
        
        // Handle employee menu selection
        int choice = console.readInt("Select option: ");
        employeeController.handleMenuSelection(choice);
    }

    private void handleDataManagement() {
        console.printInfo("📂 Data Management - Feature coming soon...");
    }

    private void handleSecurityAudit() {
        console.printInfo("🛡️ Security & Audit - Feature coming soon...");
    }

    private void handleAccountSettings() {
        console.printInfo("⚙️ Account Settings - Feature coming soon...");
    }

    private void handleLogout() {
        UserSession sess = sessionManager.getCurrentSession();
        String uname = (sess != null) ? sess.getUsername() : "unknown";
        logger.info("Admin {} logging out", uname);
        
        sessionManager.clearSession();
        console.printSuccess("👋 Logged out successfully. Thank you!");
    }

    private User getCurrentUser(UserSession session) {
        if (session == null || session.getUserId() == null) {
            throw new IllegalStateException("No active session/user available");
        }
        return userRepository.findById(session.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found for current session"));
    }
}
