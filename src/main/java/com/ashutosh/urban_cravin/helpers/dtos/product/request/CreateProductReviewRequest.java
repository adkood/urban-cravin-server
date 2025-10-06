package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import jakarta.validation.constraints.*;
        import lombok.Data;

import java.util.UUID;

@Data
public class CreateProductReviewRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

//    @NotNull(message = "User ID is required")
//    private UUID userId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;

    @Size(max = 1000, message = "Review cannot exceed 1000 characters")
    private String comment;
}
