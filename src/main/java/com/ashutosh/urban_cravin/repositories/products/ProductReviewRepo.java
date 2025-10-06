package com.ashutosh.urban_cravin.repositories.products;

import com.ashutosh.urban_cravin.models.product.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductReviewRepo extends JpaRepository<ProductReview, UUID> {

    List<ProductReview> findByProductId(UUID productId);
    List<ProductReview> findByUserId(UUID userId);
}
