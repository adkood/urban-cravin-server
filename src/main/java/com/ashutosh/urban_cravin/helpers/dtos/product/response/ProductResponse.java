package com.ashutosh.urban_cravin.helpers.dtos.product.response;

import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.models.product.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private Integer discountPercentage;
    private Integer taxPercentage;
    private boolean active;
    private int stockQuantity;
    private String sku;

    private ProductCategoryResponse category;
    private List<ProductImageResponse> images;

    public static ProductResponse toProductDto(Product product) {
        ProductCategoryResponse categoryDto = new ProductCategoryResponse(
                product.getProductCategory().getId(),
                product.getProductCategory().getName(),
                product.getProductCategory().getSlug()
        );

//        ProductImage primaryImage = product.getImages().stream()
//                .filter(ProductImage::isPrimaryImage)
//                .findFirst()
//                .orElse(null);

        List<ProductImageResponse> imageDtos = new ArrayList<>();
        for(ProductImage img: product.getImages()) {
            imageDtos.add(new ProductImageResponse(
                    img.getId(),
                    img.getUrl(),
                    img.isPrimaryImage(),
                    img.getAltText()
            ));
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountAmount(),
                product.getDiscountPercentage(),
                product.getTaxPercentage(),
                product.isActive(),
                product.getStockQuantity(),
                product.getSku(),
                categoryDto,
                imageDtos
        );
    }
}
