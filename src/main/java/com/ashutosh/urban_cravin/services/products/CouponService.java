package com.ashutosh.urban_cravin.services.products;

import com.ashutosh.urban_cravin.helpers.enums.CouponScope;
import com.ashutosh.urban_cravin.models.product.Coupon;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.repositories.products.CouponRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    @Autowired
    private CouponRepo couponRepo;

    @Autowired
    private ProductRepo productRepo;

    public Coupon addCoupon(Coupon coupon, UUID productId) {
        if (couponRepo.existsByCode(coupon.getCode())) {
            throw new RuntimeException("Coupon code already exists");
        }

        if (productId != null) {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            coupon.setProduct(product);
        }

        return couponRepo.save(coupon);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepo.findAll();
    }

    public Coupon getCouponById(UUID id) {
        return couponRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    public void deleteCoupon(UUID id) {
        Coupon coupon = getCouponById(id);
        couponRepo.delete(coupon);
    }
    public Coupon validateAndGet(String code) {
        Coupon coupon = couponRepo.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        if (!coupon.isActive()) throw new RuntimeException("Coupon is not active");
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom()))
            throw new RuntimeException("Coupon not valid yet");
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil()))
            throw new RuntimeException("Coupon expired");
        if (coupon.getUsedCount() >= coupon.getUsageLimit())
            throw new RuntimeException("Coupon usage limit reached");

        return coupon;
    }

    /**
     * Given a coupon and a product + baseAmount (price after dev discount but before tax),
     * return the discount amount to apply on that baseAmount.
     */
    public BigDecimal computeItemCouponDiscount(Coupon coupon, Product product, BigDecimal baseAmount) {

        // determine if coupon applies to this product
        boolean applies = false;
        if (coupon.getScope() == CouponScope.GLOBAL) applies = true;
        else if (coupon.getScope() == CouponScope.PRODUCT && coupon.getProduct() != null
                && coupon.getProduct().getId().equals(product.getId())) applies = true;
        else if (coupon.getScope() == CouponScope.CATEGORY && coupon.getCategory() != null
                && product.getProductCategory() != null
                && coupon.getCategory().getId().equals(product.getProductCategory().getId())) applies = true;

        if (!applies) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountAmount() != null && coupon.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = coupon.getDiscountAmount();
        } else if (coupon.getDiscountPercentage() != null && coupon.getDiscountPercentage() > 0) {
            discount = baseAmount.multiply(BigDecimal.valueOf(coupon.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100));
        }

        // don't exceed baseAmount
        if (discount.compareTo(baseAmount) > 0) return baseAmount;
        return discount;
    }

    public void incrementUsage(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepo.save(coupon);
    }

    public void decrementUsage(Coupon coupon) {
        coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
        couponRepo.save(coupon);
    }
}
