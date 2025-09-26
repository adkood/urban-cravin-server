package com.ashutosh.urban_cravin.helpers.dtos.product.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private UUID id;
    private String url;
    private boolean primaryImage;
    private String altText;
}
