package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import com.ashutosh.urban_cravin.helpers.enums.CouponScope;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateCouponRequest {

    @NotBlank
    private String code;

    private BigDecimal discountAmount;

    @Min(0)
    @Max(100)
    private Integer discountPercentage;

    @FutureOrPresent
    private LocalDateTime validFrom;

    @Future
    private LocalDateTime validUntil;

    private boolean active = true;

    @Min(1)
    private int usageLimit = 1;

    private CouponScope scope = CouponScope.GLOBAL;

    // Optional → for PRODUCT-specific coupons
    private UUID productId;

    // Optional → for CATEGORY-specific coupons
    private UUID categoryId;
}
