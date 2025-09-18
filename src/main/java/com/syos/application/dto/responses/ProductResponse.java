package com.syos.application.dto.responses;

/**
 * Product Response DTO for product management operations
 * Provides consistent response format for all product-related operations
 */
public class ProductResponse {
    private final boolean success;
    private final String message;
    private final Long productId;
    private final String itemCode;
    private final String itemName;
    private final String error;

    private ProductResponse(boolean success, String message, Long productId, 
                           String itemCode, String itemName, String error) {
        this.success = success;
        this.message = message;
        this.productId = productId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.error = error;
    }

    // Factory methods for success responses
    public static ProductResponse success(Long productId, String message, String itemCode, String itemName) {
        return new ProductResponse(true, message, productId, itemCode, itemName, null);
    }

    public static ProductResponse success(String message) {
        return new ProductResponse(true, message, null, null, null, null);
    }

    // Factory methods for failure responses
    public static ProductResponse failure(String error) {
        return new ProductResponse(false, null, null, null, null, error);
    }

    public static ProductResponse failure(String error, String itemCode) {
        return new ProductResponse(false, null, null, itemCode, null, error);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getProductId() { return productId; }
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getError() { return error; }

    public boolean isFailure() { return !success; }

    @Override
    public String toString() {
        if (success) {
            return String.format("ProductResponse{success=true, message='%s', itemCode='%s', itemName='%s'}", 
                    message, itemCode, itemName);
        } else {
            return String.format("ProductResponse{success=false, error='%s'}", error);
        }
    }
}
