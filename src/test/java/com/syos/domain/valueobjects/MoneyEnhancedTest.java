package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Money value object
 * Tests all arithmetic operations, validation, formatting, and edge cases
 * 
 * Target: 100% line coverage for Money value object
 */
@DisplayName("Money Value Object Comprehensive Tests")
class MoneyEnhancedTest {
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create Money with valid positive amount")
        void shouldCreateMoneyWithValidPositiveAmount() {
            Money money = new Money(BigDecimal.valueOf(10.50));
            assertEquals(BigDecimal.valueOf(10.50), money.getAmount());
        }
        
        @Test
        @DisplayName("Should create Money with zero amount")
        void shouldCreateMoneyWithZeroAmount() {
            Money money = new Money(BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO, money.getAmount());
            assertEquals(Money.ZERO, money);
        }
        
        @Test
        @DisplayName("Should create Money with very small amount")
        void shouldCreateMoneyWithVerySmallAmount() {
            Money money = new Money(new BigDecimal("0.01"));
            assertEquals(new BigDecimal("0.01"), money.getAmount());
        }
        
        @Test
        @DisplayName("Should create Money with large amount")
        void shouldCreateMoneyWithLargeAmount() {
            BigDecimal largeAmount = new BigDecimal("999999999.99");
            Money money = new Money(largeAmount);
            assertEquals(largeAmount, money.getAmount());
        }
        
        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Money(null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Money(BigDecimal.valueOf(-1.0));
            });
        }
        
        @Test
        @DisplayName("Should handle decimal precision correctly")
        void shouldHandleDecimalPrecisionCorrectly() {
            Money money = new Money(new BigDecimal("10.123456789"));
            // Should preserve precision
            assertEquals(new BigDecimal("10.123456789"), money.getAmount());
        }
    }
    
    @Nested
    @DisplayName("Arithmetic Operations Tests")
    class ArithmeticOperationsTests {
        
        private final Money ten = new Money(BigDecimal.valueOf(10.00));
        private final Money five = new Money(BigDecimal.valueOf(5.00));
        private final Money zero = Money.ZERO;
        
        @Test
        @DisplayName("Should add money correctly")
        void shouldAddMoneyCorrectly() {
            Money result = ten.add(five);
            assertEquals(new Money(BigDecimal.valueOf(15.00)), result);
        }
        
        @Test
        @DisplayName("Should subtract money correctly")
        void shouldSubtractMoneyCorrectly() {
            Money result = ten.subtract(five);
            assertEquals(new Money(BigDecimal.valueOf(5.00)), result);
        }
        
        @Test
        @DisplayName("Should multiply by integer correctly")
        void shouldMultiplyByIntegerCorrectly() {
            Money result = five.multiply(3);
            assertEquals(new Money(BigDecimal.valueOf(15.00)), result);
        }
        
        @Test
        @DisplayName("Should multiply by BigDecimal correctly")
        void shouldMultiplyByBigDecimalCorrectly() {
            Money result = ten.multiply(new BigDecimal("1.5"));
            assertEquals(new Money(BigDecimal.valueOf(15.00)), result);
        }
        
        @Test
        @DisplayName("Should divide by integer correctly")
        void shouldDivideByIntegerCorrectly() {
            Money result = ten.divide(2);
            assertEquals(new Money(BigDecimal.valueOf(5.00)), result);
        }
        
        @Test
        @DisplayName("Should divide by BigDecimal correctly")
        void shouldDivideByBigDecimalCorrectly() {
            Money result = ten.divide(new BigDecimal("2.0"));
            assertEquals(new Money(BigDecimal.valueOf(5.00)), result);
        }
        
        @Test
        @DisplayName("Should handle division with rounding")
        void shouldHandleDivisionWithRounding() {
            Money money = new Money(BigDecimal.valueOf(10.00));
            Money result = money.divide(3);
            // Should round to 2 decimal places
            assertEquals(new Money(new BigDecimal("3.33")), result);
        }
        
        @Test
        @DisplayName("Should add zero correctly")
        void shouldAddZeroCorrectly() {
            Money result = ten.add(zero);
            assertEquals(ten, result);
        }
        
        @Test
        @DisplayName("Should subtract zero correctly")
        void shouldSubtractZeroCorrectly() {
            Money result = ten.subtract(zero);
            assertEquals(ten, result);
        }
        
        @Test
        @DisplayName("Should multiply by zero correctly")
        void shouldMultiplyByZeroCorrectly() {
            Money result = ten.multiply(0);
            assertEquals(Money.ZERO, result);
        }
        
        @Test
        @DisplayName("Should multiply by one correctly")
        void shouldMultiplyByOneCorrectly() {
            Money result = ten.multiply(1);
            assertEquals(ten, result);
        }
        
        @Test
        @DisplayName("Should throw exception when adding null")
        void shouldThrowExceptionWhenAddingNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.add(null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when subtracting null")
        void shouldThrowExceptionWhenSubtractingNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.subtract(null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when result is negative after subtraction")
        void shouldThrowExceptionWhenResultIsNegativeAfterSubtraction() {
            assertThrows(IllegalArgumentException.class, () -> {
                five.subtract(ten); // 5 - 10 = -5 (negative)
            });
        }
        
        @Test
        @DisplayName("Should throw exception when dividing by zero")
        void shouldThrowExceptionWhenDividingByZero() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.divide(0);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.divide(BigDecimal.ZERO);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when multiplying by negative number")
        void shouldThrowExceptionWhenMultiplyingByNegativeNumber() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.multiply(-1);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.multiply(BigDecimal.valueOf(-1.0));
            });
        }
        
        @Test
        @DisplayName("Should throw exception when dividing by negative number")
        void shouldThrowExceptionWhenDividingByNegativeNumber() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.divide(-1);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.divide(BigDecimal.valueOf(-1.0));
            });
        }
    }
    
    @Nested
    @DisplayName("Comparison Operations Tests")
    class ComparisonOperationsTests {
        
        private final Money ten = new Money(BigDecimal.valueOf(10.00));
        private final Money five = new Money(BigDecimal.valueOf(5.00));
        private final Money anotherTen = new Money(BigDecimal.valueOf(10.00));
        private final Money zero = Money.ZERO;
        
        @Test
        @DisplayName("Should compare greater than correctly")
        void shouldCompareGreaterThanCorrectly() {
            assertTrue(ten.isGreaterThan(five));
            assertFalse(five.isGreaterThan(ten));
            assertFalse(ten.isGreaterThan(anotherTen));
        }
        
        @Test
        @DisplayName("Should compare greater than or equal correctly")
        void shouldCompareGreaterThanOrEqualCorrectly() {
            assertTrue(ten.isGreaterThanOrEqualTo(five));
            assertTrue(ten.isGreaterThanOrEqualTo(anotherTen));
            assertFalse(five.isGreaterThanOrEqualTo(ten));
        }
        
        @Test
        @DisplayName("Should compare less than correctly")
        void shouldCompareLessThanCorrectly() {
            assertTrue(five.isLessThan(ten));
            assertFalse(ten.isLessThan(five));
            assertFalse(ten.isLessThan(anotherTen));
        }
        
        @Test
        @DisplayName("Should compare less than or equal correctly")
        void shouldCompareLessThanOrEqualCorrectly() {
            assertTrue(five.isLessThanOrEqualTo(ten));
            assertTrue(ten.isLessThanOrEqualTo(anotherTen));
            assertFalse(ten.isLessThanOrEqualTo(five));
        }
        
        @Test
        @DisplayName("Should identify zero correctly")
        void shouldIdentifyZeroCorrectly() {
            assertTrue(zero.isZero());
            assertFalse(ten.isZero());
            assertTrue(new Money(BigDecimal.ZERO).isZero());
        }
        
        @Test
        @DisplayName("Should identify positive values correctly")
        void shouldIdentifyPositiveValuesCorrectly() {
            assertTrue(ten.isPositive());
            assertTrue(five.isPositive());
            assertFalse(zero.isPositive());
        }
        
        @Test
        @DisplayName("Should throw exception when comparing with null")
        void shouldThrowExceptionWhenComparingWithNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                ten.isGreaterThan(null);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.isLessThan(null);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.isGreaterThanOrEqualTo(null);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                ten.isLessThanOrEqualTo(null);
            });
        }
    }
    
    @Nested
    @DisplayName("Equality and Hash Code Tests")
    class EqualityAndHashCodeTests {
        
        @Test
        @DisplayName("Should be equal when amounts are the same")
        void shouldBeEqualWhenAmountsAreTheSame() {
            Money money1 = new Money(BigDecimal.valueOf(10.00));
            Money money2 = new Money(BigDecimal.valueOf(10.00));
            
            assertEquals(money1, money2);
            assertEquals(money1.hashCode(), money2.hashCode());
        }
        
        @Test
        @DisplayName("Should be equal when amounts are mathematically equal")
        void shouldBeEqualWhenAmountsAreMathematicallyEqual() {
            Money money1 = new Money(new BigDecimal("10.00"));
            Money money2 = new Money(new BigDecimal("10.0"));
            
            assertEquals(money1, money2);
            assertEquals(money1.hashCode(), money2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when amounts are different")
        void shouldNotBeEqualWhenAmountsAreDifferent() {
            Money money1 = new Money(BigDecimal.valueOf(10.00));
            Money money2 = new Money(BigDecimal.valueOf(5.00));
            
            assertNotEquals(money1, money2);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Money money = new Money(BigDecimal.valueOf(10.00));
            assertNotEquals(money, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            Money money = new Money(BigDecimal.valueOf(10.00));
            assertNotEquals(money, "10.00");
            assertNotEquals(money, 10.00);
            assertNotEquals(money, BigDecimal.valueOf(10.00));
        }
        
        @Test
        @DisplayName("Should be reflexive")
        void shouldBeReflexive() {
            Money money = new Money(BigDecimal.valueOf(10.00));
            assertEquals(money, money);
        }
        
        @Test
        @DisplayName("Should be symmetric")
        void shouldBeSymmetric() {
            Money money1 = new Money(BigDecimal.valueOf(10.00));
            Money money2 = new Money(BigDecimal.valueOf(10.00));
            
            assertEquals(money1, money2);
            assertEquals(money2, money1);
        }
        
        @Test
        @DisplayName("Should be transitive")
        void shouldBeTransitive() {
            Money money1 = new Money(BigDecimal.valueOf(10.00));
            Money money2 = new Money(BigDecimal.valueOf(10.00));
            Money money3 = new Money(BigDecimal.valueOf(10.00));
            
            assertEquals(money1, money2);
            assertEquals(money2, money3);
            assertEquals(money1, money3);
        }
    }
    
    @Nested
    @DisplayName("Formatting and String Representation Tests")
    class FormattingAndStringRepresentationTests {
        
        @Test
        @DisplayName("Should format as currency string")
        void shouldFormatAsCurrencyString() {
            Money money = new Money(BigDecimal.valueOf(10.50));
            String formatted = money.toCurrencyString();
            
            assertNotNull(formatted);
            assertTrue(formatted.contains("10.50"));
        }
        
        @Test
        @DisplayName("Should format zero as currency string")
        void shouldFormatZeroAsCurrencyString() {
            String formatted = Money.ZERO.toCurrencyString();
            
            assertNotNull(formatted);
            assertTrue(formatted.contains("0.00"));
        }
        
        @Test
        @DisplayName("Should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            Money money = new Money(BigDecimal.valueOf(10.50));
            String toString = money.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("Money"));
            assertTrue(toString.contains("10.50"));
        }
        
        @Test
        @DisplayName("Should handle large numbers in string representation")
        void shouldHandleLargeNumbersInStringRepresentation() {
            Money money = new Money(new BigDecimal("1234567.89"));
            String toString = money.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("1234567.89"));
        }
    }
    
    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {
        
        @Test
        @DisplayName("Should create Money from double")
        void shouldCreateMoneyFromDouble() {
            Money money = Money.of(10.50);
            assertEquals(new Money(BigDecimal.valueOf(10.50)), money);
        }
        
        @Test
        @DisplayName("Should create Money from int")
        void shouldCreateMoneyFromInt() {
            Money money = Money.of(10);
            assertEquals(new Money(BigDecimal.valueOf(10.00)), money);
        }
        
        @Test
        @DisplayName("Should create Money from string")
        void shouldCreateMoneyFromString() {
            Money money = Money.of("10.50");
            assertEquals(new Money(new BigDecimal("10.50")), money);
        }
        
        @Test
        @DisplayName("Should throw exception when creating from negative double")
        void shouldThrowExceptionWhenCreatingFromNegativeDouble() {
            assertThrows(IllegalArgumentException.class, () -> {
                Money.of(-10.50);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when creating from negative int")
        void shouldThrowExceptionWhenCreatingFromNegativeInt() {
            assertThrows(IllegalArgumentException.class, () -> {
                Money.of(-10);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when creating from invalid string")
        void shouldThrowExceptionWhenCreatingFromInvalidString() {
            assertThrows(NumberFormatException.class, () -> {
                Money.of("invalid");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when creating from null string")
        void shouldThrowExceptionWhenCreatingFromNullString() {
            assertThrows(IllegalArgumentException.class, () -> {
                Money.of((String) null);
            });
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Special Scenarios Tests")
    class EdgeCasesAndSpecialScenariosTests {
        
        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            BigDecimal largeAmount = new BigDecimal("999999999999999999.99");
            Money money = new Money(largeAmount);
            
            assertEquals(largeAmount, money.getAmount());
        }
        
        @Test
        @DisplayName("Should handle very small positive numbers")
        void shouldHandleVerySmallPositiveNumbers() {
            BigDecimal smallAmount = new BigDecimal("0.000000001");
            Money money = new Money(smallAmount);
            
            assertEquals(smallAmount, money.getAmount());
        }
        
        @Test
        @DisplayName("Should handle precision in calculations")
        void shouldHandlePrecisionInCalculations() {
            Money money1 = new Money(new BigDecimal("0.1"));
            Money money2 = new Money(new BigDecimal("0.2"));
            Money money3 = new Money(new BigDecimal("0.3"));
            
            Money result = money1.add(money2);
            assertEquals(money3, result);
        }
        
        @Test
        @DisplayName("Should handle chained operations")
        void shouldHandleChainedOperations() {
            Money money = new Money(BigDecimal.valueOf(100.00));
            Money result = money
                .add(new Money(BigDecimal.valueOf(50.00)))
                .subtract(new Money(BigDecimal.valueOf(25.00)))
                .multiply(2)
                .divide(5);
            
            assertEquals(new Money(BigDecimal.valueOf(50.00)), result);
        }
        
        @Test
        @DisplayName("Should maintain immutability")
        void shouldMaintainImmutability() {
            Money original = new Money(BigDecimal.valueOf(100.00));
            Money modified = original.add(new Money(BigDecimal.valueOf(50.00)));
            
            assertEquals(new Money(BigDecimal.valueOf(100.00)), original);
            assertEquals(new Money(BigDecimal.valueOf(150.00)), modified);
            assertNotSame(original, modified);
        }
        
        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            Money money = new Money(BigDecimal.valueOf(100.00));
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            Money[] results = new Money[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = money.add(new Money(BigDecimal.valueOf(index + 1)));
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify original is unchanged and all results are correct
            assertEquals(new Money(BigDecimal.valueOf(100.00)), money);
            for (int i = 0; i < threadCount; i++) {
                assertEquals(new Money(BigDecimal.valueOf(100.00 + i + 1)), results[i]);
            }
        }
    }
}
