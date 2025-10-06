package com.ashutosh.urban_cravin.models.product;

import com.ashutosh.urban_cravin.helpers.enums.ProductSize;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @Min(0) @Max(100)
    private Integer discountPercentage;

    @Min(0) @Max(100)
    private Integer taxPercentage;

    private boolean active = true;

    @Min(0)
    private int stockQuantity;

    private Double weight; // for shipping calc

    @Size(max = 255, message = "Dimensions text too long")
    private String dimensions; // e.g. "10x20x5 cm"

    @NotNull(message = "Size is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSize size;

    @NotBlank(message = "SKU is required")
    @Column(unique = true)
    private String sku; // unique product code

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version;  // optimistic locking for stock updates

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false, columnDefinition = "CHAR(36)")
    private ProductCategory productCategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coupon> coupons;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReview> reviews;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPriceAfterDeveloperDiscount() {
        BigDecimal effective = price;

        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            effective = effective.subtract(discountAmount);
        }

        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal pct = effective.multiply(BigDecimal.valueOf(discountPercentage))
                    .divide(BigDecimal.valueOf(100));
            effective = effective.subtract(pct);
        }

        if (effective.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        return effective;
    }

    public BigDecimal computeTax(BigDecimal taxableAmount) {
        if (taxPercentage == null || taxPercentage == 0) return BigDecimal.ZERO;
        return taxableAmount.multiply(BigDecimal.valueOf(taxPercentage))
                .divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getFinalPriceWithTax() {
        BigDecimal afterDisc = getPriceAfterDeveloperDiscount();
        BigDecimal tax = computeTax(afterDisc);
        return afterDisc.add(tax);
    }
}
