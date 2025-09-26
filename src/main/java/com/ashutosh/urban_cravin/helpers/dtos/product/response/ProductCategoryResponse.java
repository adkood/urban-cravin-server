package com.ashutosh.urban_cravin.helpers.dtos.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {
    private UUID id;
    private String name;
    private String slug;
}

