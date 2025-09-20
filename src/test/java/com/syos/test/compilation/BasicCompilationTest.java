package com.syos.test.compilation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.SynexPoints;
import com.syos.domain.entities.User;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic compilation verification test
 * Tests that key classes can be instantiated without compilation errors
 */
@DisplayName("Basic Compilation Verification Test")
public class BasicCompilationTest {
    
    @Test
    @DisplayName("Should create Money objects without compilation errors")
    void shouldCreateMoneyObjectsWithoutCompilationErrors() {
        Money money1 = Money.of(10.50);
        Money money2 = Money.of(BigDecimal.valueOf(25.75));
        Money zero = Money.zero();
        
        assertNotNull(money1);
        assertNotNull(money2);
        assertNotNull(zero);
        
        // Test arithmetic operations
        Money sum = money1.add(money2);
        assertNotNull(sum);
        
        // Test comparison operations
        assertTrue(money1.isPositive());
        assertTrue(zero.isZero());
    }
    
    @Test
    @DisplayName("Should create SynexPoints objects without compilation errors")
    void shouldCreateSynexPointsObjectsWithoutCompilationErrors() {
        SynexPoints points1 = SynexPoints.of(BigDecimal.valueOf(100));
        SynexPoints points2 = SynexPoints.of(BigDecimal.valueOf(25));
        SynexPoints zero = SynexPoints.zero();
        
        assertNotNull(points1);
        assertNotNull(points2);
        assertNotNull(zero);
        
        // Test arithmetic operations
        SynexPoints sum = points1.add(points2);
        assertNotNull(sum);
        
        // Test comparison operations
        assertTrue(zero.isZero());
    }
    
    @Test
    @DisplayName("Should verify all key classes can be referenced")
    void shouldVerifyAllKeyClassesCanBeReferenced() {
        // Just verify that the classes exist and can be referenced
        assertNotNull(Money.class);
        assertNotNull(SynexPoints.class);
        assertNotNull(User.class);
        assertNotNull(LoginUseCase.class);
        assertNotNull(RegisterCustomerUseCase.class);
    }
}
