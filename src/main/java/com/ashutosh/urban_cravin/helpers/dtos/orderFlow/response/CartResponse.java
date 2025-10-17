package com.ashutosh.urban_cravin.helpers.dtos.orderFlow.response;

import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.models.orderFlow.CartItem;
import com.ashutosh.urban_cravin.models.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID id;
    private UUID userId;
    private String username;

    // Cart-wide totals
    private BigDecimal cartTotalPrice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDTO {
        private UUID id;
        private Integer quantity;
        private BigDecimal unitPrice;       // per product unit (after discount & tax)
        private BigDecimal itemTotalPrice;  // unitPrice * quantity
        private ProductDTO product;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private UUID id;
        private String name;
        private BigDecimal price;              // base price
        private BigDecimal discountAmount;
        private Integer discountPercentage;
        private Integer taxPercentage;
        private boolean active;
        private int stockQuantity;
        private Double weight;
        private String sku;
    }

    public static CartResponse mapToCartDto(Cart cart) {
        List<CartItemDTO> itemDtos = cart.getItems().stream()
                .map(CartResponse::mapItemToDTO)
                .toList();

        // Calculate cart total price using BigDecimal
        BigDecimal cartTotal = itemDtos.stream()
                .map(CartItemDTO::getItemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getUser().getUsername(),
                cartTotal,
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                itemDtos
        );
    }

    private static CartItemDTO mapItemToDTO(CartItem item) {
        Product product = item.getProduct();

        BigDecimal basePrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal discountedPrice = basePrice;

        // Apply discount percentage
        if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
            BigDecimal discountPercent = BigDecimal.valueOf(product.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100));
            discountedPrice = discountedPrice.subtract(discountedPrice.multiply(discountPercent));
        }
        // Apply discount amount
        else if (product.getDiscountAmount() != null && product.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountedPrice = discountedPrice.subtract(product.getDiscountAmount());
        }

        // Apply tax percentage
        if (product.getTaxPercentage() != null && product.getTaxPercentage() > 0) {
            BigDecimal taxPercent = BigDecimal.valueOf(product.getTaxPercentage())
                    .divide(BigDecimal.valueOf(100));
            discountedPrice = discountedPrice.add(discountedPrice.multiply(taxPercent));
        }

        // Ensure non-negative
        BigDecimal unitPrice = discountedPrice.max(BigDecimal.ZERO);

        // Item total = unitPrice * quantity
        BigDecimal itemTotalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        ProductDTO productDto = new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDiscountAmount(),
                product.getDiscountPercentage(),
                product.getTaxPercentage(),
                product.isActive(),
                product.getStockQuantity(),
                product.getWeight(),
                product.getSku()
        );

        return new CartItemDTO(
                item.getId(),
                item.getQuantity(),
                unitPrice,
                itemTotalPrice,
                productDto
        );
    }
}

//package com.ashutosh.urban_cravin.helpers.dtos.orderFlow.response;
//
//import com.ashutosh.urban_cravin.models.orderFlow.Cart;
//import com.ashutosh.urban_cravin.models.orderFlow.CartItem;
//import com.ashutosh.urban_cravin.models.product.Product;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class CartResponse {
//
//    private UUID id;
//    private UUID userId;
//    private String username;
//
//    // Cart-wide totals
//    private BigDecimal cartTotalPrice;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private List<CartItemDTO> items;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class CartItemDTO {
//        private UUID id;
//        private Integer quantity;
//        private BigDecimal unitPrice;       // per product unit
//        private BigDecimal itemTotalPrice;  // unitPrice * quantity
//        private ProductDTO product;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ProductDTO {
//        private UUID id;
//        private String name;
//        private BigDecimal price;              // base price
//        private BigDecimal discountAmount;
//        private Integer discountPercentage;
//        private Integer taxPercentage;
//        private boolean active;
//        private int stockQuantity;
//        private Double weight;
//        private String sku;
//    }
//
//    public static CartResponse mapToCartDto(Cart cart) {
//        List<CartItemDTO> itemDtos = cart.getItems().stream().map(item -> mapItemToDTO(item)).toList();
//
//        // Calculate cart total price
//        double cartTotal = itemDtos.stream()
//                .mapToDouble(CartItemDTO::getItemTotalPrice)
//                .sum();
//
//        return new CartResponse(
//                cart.getId(),
//                cart.getUser().getId(),
//                cart.getUser().getUsername(),
//                cartTotal,
//                cart.getCreatedAt(),
//                cart.getUpdatedAt(),
//                itemDtos
//        );
//    }
//
//    private static CartItemDTO mapItemToDTO(CartItem item) {
//        Product product = item.getProduct();
//
//        // Base product price
//        double basePrice = product.getPrice().doubleValue();
//
//        // Apply discount (if any)
//        double discountedPrice = basePrice;
//        if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
//            discountedPrice -= (basePrice * product.getDiscountPercentage()) / 100.0;
//        } else if (product.getDiscountAmount() != null && product.getDiscountAmount().doubleValue() > 0) {
//            discountedPrice -= product.getDiscountAmount().doubleValue();
//        }
//
//        // Apply tax (if any)
//        if (product.getTaxPercentage() != null && product.getTaxPercentage() > 0) {
//            discountedPrice += (discountedPrice * product.getTaxPercentage()) / 100.0;
//        }
//
//        // Unit Price (after discount & tax)
//        double unitPrice = Math.max(discountedPrice, 0);
//
//        // Item total
//        double itemTotalPrice = unitPrice * item.getQuantity();
//
//        ProductDTO productDto = new ProductDTO(
//                product.getId(),
//                product.getName(),
//                product.getPrice(),
//                product.getDiscountAmount(),
//                product.getDiscountPercentage(),
//                product.getTaxPercentage(),
//                product.isActive(),
//                product.getStockQuantity(),
//                product.getWeight(),
//                product.getSku()
//        );
//
//        return new CartItemDTO(
//                item.getId(),
//                item.getQuantity(),
//                unitPrice,
//                itemTotalPrice,
//                productDto
//        );
//    }
//}
