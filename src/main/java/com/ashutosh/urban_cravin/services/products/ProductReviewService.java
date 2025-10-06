package com.ashutosh.urban_cravin.services.products;

import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductReviewRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductReviewRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.response.ProductReviewResponse;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.models.product.ProductReview;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.repositories.users.UserRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductReviewRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewRepo reviewRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    // Add new review
    public ProductReviewResponse addReview(CreateProductReviewRequest req, UUID userId) {
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        ProductReview saved = reviewRepo.save(review);
        return mapToResponse(saved);
    }

    // Get all reviews for a product
    public List<ProductReviewResponse> getReviewsByProduct(UUID productId) {
        return reviewRepo.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get all reviews by a user
    public List<ProductReviewResponse> getReviewsByUser(UUID userId) {
        return reviewRepo.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Update a review
    public ProductReviewResponse updateReview(UUID reviewId, UpdateProductReviewRequest req) {
        ProductReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found"));

        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());

        ProductReview updated = reviewRepo.save(review);
        return mapToResponse(updated);
    }

    // Delete a review
    public void deleteReview(UUID reviewId) {
        ProductReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found"));
        reviewRepo.delete(review);
    }

    // Helper mapper
    private ProductReviewResponse mapToResponse(ProductReview review) {
        return ProductReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getUsername())
                .build();
    }
}
