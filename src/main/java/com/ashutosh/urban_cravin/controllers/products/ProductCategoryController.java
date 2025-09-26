package com.ashutosh.urban_cravin.controllers.products;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductCategoryRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductCategoryRequest;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.product.ProductCategory;
import com.ashutosh.urban_cravin.services.products.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CreateProductCategoryRequest req) {
        ProductCategory category = productCategoryService.addProductCategory(req);
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Category created successfully", Map.of("category", category))
        );
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getAllCategories() {
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Categories fetched successfully",
                        Map.of("categories", productCategoryService.getAllCategories()))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID id) {
        ProductCategory category = productCategoryService.getCategoryById(id);
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Category fetched successfully", Map.of("category", category))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateProductCategoryRequest req) {
        ProductCategory updated = productCategoryService.updateCategory(id, req);
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Category updated successfully", Map.of("category", updated))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
        productCategoryService.deleteCategory(id);
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Category deleted successfully", null)
        );
    }
}
