 package com.ashutosh.urban_cravin.models.product;

import com.ashutosh.urban_cravin.helpers.enums.CouponScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @NotBlank
    @Column(unique = true)
    private String code;

    @DecimalMin(value = "0.0")
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

    private int usedCount = 0;

    @Enumerated(EnumType.STRING)
    private CouponScope scope = CouponScope.GLOBAL;

    // optional; used when scope == PRODUCT
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true, columnDefinition = "BINARY(16)")
    private Product product;

    // optional; used when scope == CATEGORY
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true, columnDefinition = "BiNARY(16)")
    private ProductCategory category;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}