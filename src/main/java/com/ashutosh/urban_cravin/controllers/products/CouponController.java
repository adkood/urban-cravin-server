package com.ashutosh.urban_cravin.controllers.products;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateCouponRequest;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.product.Coupon;
import com.ashutosh.urban_cravin.services.products.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/categories/products/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse> createCoupon(@RequestBody @Valid CreateCouponRequest request) {
        Coupon created = couponService.addCoupon(request);
        return ResponseEntity.ok(
                new ApiResponse(Status.Success, "Coupon created", Map.of("coupon", created))
        );
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Coupons fetched", Map.of("coupons", coupons)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCoupon(@PathVariable UUID id) {
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Coupon fetched", Map.of("coupon", coupon)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Coupon deleted", null));
    }
}

