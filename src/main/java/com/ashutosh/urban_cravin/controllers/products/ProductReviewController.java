package com.ashutosh.urban_cravin.controllers.products;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductReviewRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductReviewRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.response.ProductReviewResponse;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.users.UserPrincipal;
import com.ashutosh.urban_cravin.services.products.ProductReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse> addReview(@Valid @RequestBody CreateProductReviewRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        ProductReviewResponse dto = reviewService.addReview(req,userId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Review added", Map.of("review", dto)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getReviewsByProduct(@PathVariable UUID productId) {
        List<ProductReviewResponse> reviewsDtos = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Reviews for a product fetched successfully", Map.of("reviews", reviewsDtos)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getReviewsByUser(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        List<ProductReviewResponse> reviewDtos = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Reviews fetched successfully", Map.of("reviews", reviewDtos)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateProductReviewRequest req) {
        ProductReviewResponse dto = reviewService.updateReview(reviewId, req);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Review updated", Map.of("review", dto)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Review deleted", null));
    }
}
