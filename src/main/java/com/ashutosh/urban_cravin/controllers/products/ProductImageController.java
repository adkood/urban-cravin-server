package com.ashutosh.urban_cravin.controllers.products;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductImageRequest;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.product.ProductImage;
import com.ashutosh.urban_cravin.services.products.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/categories/products/images")
public class ProductImageController {

    @Autowired
    private ProductImageService imageService;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse> addImages(@PathVariable UUID productId, @ModelAttribute CreateProductImageRequest req) {
        ProductImage image = imageService.addImage(productId, req);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Images added", Map.of("image", image)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getImages(@PathVariable UUID productId) {
        List<ProductImage> images = imageService.getImagesByProduct(productId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Images fetched", Map.of("images", images)));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Image deleted", null));
    }
}
