package com.ashutosh.urban_cravin.controllers.products;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.response.ProductResponse;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.services.products.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/categories/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody CreateProductRequest req) {
        Product product = productService.addProduct(req);

        return ResponseEntity.ok(
                new ApiResponse(
                        Status.Success,
                        "Product created successfully",
                        Map.of("product", product)
                )
        );
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        List<ProductResponse> dtos = products.stream().map(ProductResponse::toProductDto).toList();

        return ResponseEntity.ok(
                new ApiResponse(
                        Status.Success,
                        "Products fetched successfully",
                        Map.of("products", dtos)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSingleProduct(@PathVariable UUID id) {
        Product product = productService.getProductById(id);

        return ResponseEntity.ok(
                new ApiResponse(
                        Status.Success,
                        "Product fetched successfully",
                        Map.of("product", product)
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateSingleProduct(
            @PathVariable UUID id,
            @RequestBody UpdateProductRequest req
            ) {
        Product updatedProduct = productService.updateProduct(id, req);

        return ResponseEntity.ok(
                new ApiResponse(
                        Status.Success,
                        "Product updated successfully",
                        Map.of("product", updatedProduct)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSingleProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);

        return ResponseEntity.ok(
                new ApiResponse(
                        Status.Success,
                        "Product deleted successfully",
                        null
                )
        );
    }

}
