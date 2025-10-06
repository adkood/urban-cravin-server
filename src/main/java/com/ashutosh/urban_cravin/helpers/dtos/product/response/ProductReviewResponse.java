package com.ashutosh.urban_cravin.helpers.dtos.product.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductReviewResponse {
    private UUID id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private UUID productId;
    private String productName;

    private UUID userId;
    private String userName;
}
