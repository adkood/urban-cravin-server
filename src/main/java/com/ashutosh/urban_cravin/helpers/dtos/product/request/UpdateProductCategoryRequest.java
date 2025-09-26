package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProductCategoryRequest {

    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(min = 15, max = 250, message = "Category description must be between 15 and 250 characters")
    private String description;

    private String slug;

    private Boolean active;

    private String imageUrl;

    private Integer sortOrder;

    private UUID parentCategoryId;
}
