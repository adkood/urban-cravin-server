package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import com.ashutosh.urban_cravin.helpers.enums.ProductSize;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name; // optional, only validated if provided

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description; // optional

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price; // optional

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount; // optional

    @Min(value = 0, message = "Discount percentage cannot be negative")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private Integer discountPercentage; // optional

    @Min(value = 0, message = "Tax percentage cannot be negative")
    @Max(value = 100, message = "Tax percentage cannot exceed 100")
    private Integer taxPercentage; // optional

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity; // optional

    private Double weight; // optional

    private String dimensions; // optional, e.g. "10x20x5 cm"

    private ProductSize size; // optional, must match enum

    private Boolean active; // optional

    @Size(min = 1, message = "SKU cannot be empty")
    private String sku; // optional

    private UUID categoryId; // optional
}
