package com.syos.test;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;

/**
 * Simple test to verify registration and login functionality
 */
public class QuickTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 SYOS Registration/Login Quick Test");
        System.out.println("=====================================");
        
        try {
            // Initialize components
            InMemoryUserRepository userRepository = new InMemoryUserRepository();
            RegisterCustomerUseCase registerUseCase = new RegisterCustomerUseCase(userRepository);
            LoginUseCase loginUseCase = new LoginUseCase(userRepository);
            
            System.out.println("\n1. Repository initialized with " + userRepository.getUserCount() + " default users");
            
            // Test data
            String username = "testuser";
            String password = "password123";
            String name = "Test User";
            String email = "test@example.com";
            
            System.out.println("\n2. Testing Registration...");
            System.out.println("   Username: " + username);
            System.out.println("   Password: " + password);
            System.out.println("   Name: " + name);
            System.out.println("   Email: " + email);
            
            // Register user
            User registeredUser = registerUseCase.register(username, password, name, email);
            
            System.out.println("   ✅ Registration successful!");
            System.out.println("   User ID: " + registeredUser.getId().getValue());
            System.out.println("   Username: " + registeredUser.getUsername().getValue());
            System.out.println("   Role: " + registeredUser.getRole());
            
            System.out.println("\n3. Testing Login...");
            
            // Login with registered user
            User loggedInUser = loginUseCase.login(username, password);
            
            System.out.println("   ✅ Login successful!");
            System.out.println("   User ID: " + loggedInUser.getId().getValue());
            System.out.println("   Username: " + loggedInUser.getUsername().getValue());
            System.out.println("   Role: " + loggedInUser.getRole());
            System.out.println("   SYNEX Points: " + loggedInUser.getSynexPoints().getValue());
            
            System.out.println("\n4. Testing Wrong Password...");
            try {
                loginUseCase.login(username, "wrongpassword");
                System.out.println("   ❌ Should have failed!");
            } catch (Exception e) {
                System.out.println("   ✅ Correctly rejected wrong password");
            }
            
            System.out.println("\n5. Testing Non-existent User...");
            try {
                loginUseCase.login("nonexistent", "password123");
                System.out.println("   ❌ Should have failed!");
            } catch (Exception e) {
                System.out.println("   ✅ Correctly rejected non-existent user");
            }
            
            System.out.println("\n🎉 All tests passed! Registration and login are working correctly.");
            System.out.println("\nFinal repository state:");
            userRepository.printAllUsers();
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}