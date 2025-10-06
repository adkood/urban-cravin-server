package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import com.ashutosh.urban_cravin.helpers.enums.ProductSize;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @Min(value = 0, message = "Discount percentage cannot be negative")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private Integer discountPercentage;

    @Min(value = 0, message = "Tax percentage cannot be negative")
    @Max(value = 100, message = "Tax percentage cannot exceed 100")
    private Integer taxPercentage;

    private boolean active = true;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stockQuantity;

    private Double weight;

    @Size(max = 255, message = "Dimensions cannot exceed 255 characters")
    private String dimensions;

    @NotNull(message = "Product size is required")
    private ProductSize size;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
