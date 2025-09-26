package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProductCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Category description is required")
    @Size(min = 15, max = 250, message = "Category description must be between 15 and 250 characters")
    private String description;

    @NotBlank(message = "Slug is required")
    private String slug;

    private Boolean active = true;

    private String imageUrl;

    private Integer sortOrder = 0;

    private UUID parentCategoryId;
}
