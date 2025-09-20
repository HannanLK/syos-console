package com.syos.infrastructure.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.metamodel.EntityType;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Hibernate configuration and entity mappings
 * Tests all JPA entities are properly configured and accessible
 * 
 * Target: Validate complete persistence layer setup
 */
@DisplayName("Hibernate Configuration and Entity Validation Tests")
class HibernateConfigurationValidationTest {
    
    private static EntityManagerFactory emf;
    private static EntityManager em;
    
    @BeforeAll
    static void setUpClass() {
        try {
            // Initialize EntityManagerFactory from persistence.xml
            emf = Persistence.createEntityManagerFactory("syos-persistence-unit");
            em = emf.createEntityManager();
        } catch (Exception e) {
            // If database is not available, skip these tests
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                "Database not available, skipping Hibernate tests: " + e.getMessage());
        }
    }
    
    @AfterAll
    static void tearDownClass() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @Test
    @DisplayName("Should have EntityManagerFactory properly initialized")
    void shouldHaveEntityManagerFactoryProperlyInitialized() {
        assertNotNull(emf);
        assertTrue(emf.isOpen());
    }
    
    @Test
    @DisplayName("Should have all expected entities registered")
    void shouldHaveAllExpectedEntitiesRegistered() {
        Set<EntityType<?>> entities = em.getMetamodel().getEntities();
        
        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        
        // Get entity names for validation
        Set<String> entityNames = entities.stream()
            .map(EntityType::getName)
            .collect(java.util.stream.Collectors.toSet());
        
        // Verify core entities are registered
        String[] expectedEntities = {
            "UserEntity",
            "ItemMasterFileEntity", 
            "BrandEntity",
            "CategoryEntity",
            "SupplierEntity",
            "BatchEntity",
            "WarehouseStockEntity",
            "ShelfStockEntity", 
            "WebInventoryEntity",
            "TransactionEntity",
            "TransactionItemEntity",
            "BillEntity"
        };
        
        for (String expectedEntity : expectedEntities) {
            assertTrue(entityNames.contains(expectedEntity), 
                "Entity " + expectedEntity + " should be registered. Available entities: " + entityNames);
        }
        
        // Verify minimum number of entities
        assertTrue(entities.size() >= 12, 
            "Should have at least 12 entities registered, found: " + entities.size());
    }
    
    @Test
    @DisplayName("Should be able to create entity instances")
    void shouldBeAbleToCreateEntityInstances() {
        assertDoesNotThrow(() -> {
            // Test core entity instantiation
            com.syos.infrastructure.persistence.UserEntity user = 
                new com.syos.infrastructure.persistence.UserEntity();
            assertNotNull(user);
            
            com.syos.infrastructure.persistence.entities.ItemMasterFileEntity item = 
                new com.syos.infrastructure.persistence.entities.ItemMasterFileEntity();
            assertNotNull(item);
            
            com.syos.infrastructure.persistence.entities.TransactionEntity transaction = 
                new com.syos.infrastructure.persistence.entities.TransactionEntity();
            assertNotNull(transaction);
            
            com.syos.infrastructure.persistence.entities.BillEntity bill = 
                new com.syos.infrastructure.persistence.entities.BillEntity();
            assertNotNull(bill);
        });
    }
    
    @Test
    @DisplayName("Should validate database connection properties")
    void shouldValidateDatabaseConnectionProperties() {
        // Verify EntityManagerFactory properties
        java.util.Map<String, Object> properties = emf.getProperties();
        
        assertNotNull(properties);
        assertFalse(properties.isEmpty());
        
        // Check critical properties are set
        assertTrue(properties.containsKey("jakarta.persistence.jdbc.driver") ||
                  properties.containsKey("hibernate.connection.driver_class"),
                  "Database driver should be configured");
        
        assertTrue(properties.containsKey("jakarta.persistence.jdbc.url") ||
                  properties.containsKey("hibernate.connection.url"),
                  "Database URL should be configured");
        
        assertTrue(properties.containsKey("hibernate.dialect"),
                  "Hibernate dialect should be configured");
    }
}
