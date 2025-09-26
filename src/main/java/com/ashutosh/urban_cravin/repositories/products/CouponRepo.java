package com.ashutosh.urban_cravin.repositories.products;

import com.ashutosh.urban_cravin.models.product.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepo extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);
}
