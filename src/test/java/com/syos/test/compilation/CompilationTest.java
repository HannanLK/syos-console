package com.syos.test.compilation;

import com.syos.domain.entities.User;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import com.syos.shared.enums.UnitOfMeasure;

/**
 * Simple compilation test to verify our fixes work
 */
public class CompilationTest {
    
    public void testUserCreation() {
        // Test the constructor that the tests expect
        Username username = new Username("testuser");
        Password password = new Password("TestPassword123!");
        Name name = new Name("Test User");
        Email email = new Email("test@example.com");
        
        User user = new User(username, password, name, email, UserRole.CUSTOMER);
        
        // Test role checking methods
        boolean isCustomer = user.isCustomer();
        boolean isEmployee = user.isEmployee();
        boolean isAdmin = user.isAdmin();
        
        // Test authentication
        boolean auth = user.authenticate(password);
        
        // Test SynexPoints operations
        user.addSynexPoints(SynexPoints.ZERO);
        user.redeemSynexPoints(SynexPoints.ZERO);
    }
    
    public void testItemMasterFileCreation() {
        // Test the constructor that the tests expect
        ItemCode itemCode = new ItemCode("IT001");
        Name itemName = new Name("Test Product");
        BrandId brandId = new BrandId(1L);
        CategoryId categoryId = new CategoryId(1L);
        String description = "Test Description";
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.EACH;
        PackSize packSize = new PackSize(1);
        Money costPrice = new Money(java.math.BigDecimal.valueOf(10.00));
        Money sellingPrice = new Money(java.math.BigDecimal.valueOf(15.00));
        ReorderPoint reorderPoint = new ReorderPoint(50);
        SupplierId supplierId = new SupplierId(1L);
        
        ItemMasterFile item = new ItemMasterFile(
            itemCode, itemName, brandId, categoryId, description,
            unitOfMeasure, packSize, costPrice, sellingPrice, reorderPoint, supplierId
        );
        
        // Test methods that the tests expect
        Name name = item.getName();
        Money profitMargin = item.getProfitMargin();
        java.math.BigDecimal profitMarginPercentage = item.getProfitMarginPercentage();
        
        // Test update methods
        item.updateSellingPrice(new Money(java.math.BigDecimal.valueOf(20.00)));
        item.updateDescription("New description");
        item.updateReorderPoint(new ReorderPoint(100));
        item.deactivate();
        item.reactivate();
    }
    
    public void testValueObjectsWithPublicConstructors() {
        // Test all value objects have working public constructors
        Money money = new Money(java.math.BigDecimal.valueOf(100));
        PackSize packSize = new PackSize(5);
        ReorderPoint reorderPoint = new ReorderPoint(25);
        Username username = new Username("user");
        Password password = new Password("password123");
        Name name = new Name("Name");
        Email email = new Email("email@test.com");
        ItemCode itemCode = new ItemCode("CODE001");
        BrandId brandId = new BrandId(1L);
        CategoryId categoryId = new CategoryId(1L);
        SupplierId supplierId = new SupplierId(1L);
        
        // Test constants
        SynexPoints zero = SynexPoints.ZERO;
        Money zeroMoney = Money.ZERO;
    }
}
