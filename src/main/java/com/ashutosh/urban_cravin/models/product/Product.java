 package com.ashutosh.urban_cravin.models.product;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
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

    @Min(0)
    @Max(100)
    private Integer discountPercentage;

    @Min(0)
    @Max(100)
    private Integer taxPercentage;

    private boolean active = true;

    @Min(0)
    private int stockQuantity;

    private Double weight; // for shipping calc
    private String dimensions; // e.g. "10x20x5 cm"

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
    @JoinColumn(name = "category_id", nullable = false, columnDefinition = "BINARY(16)")
    private ProductCategory productCategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coupon> coupons;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Price after developer/admin discount (but before tax and user coupon).
     */
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

    /**
     * Compute tax amount for a given taxable amount (rounded as BigDecimal rules you prefer).
     */
    public BigDecimal computeTax(BigDecimal taxableAmount) {
        if (taxPercentage == null || taxPercentage == 0) return BigDecimal.ZERO;
        return taxableAmount.multiply(BigDecimal.valueOf(taxPercentage))
                .divide(BigDecimal.valueOf(100));
    }

    /**
     * Returns final price after dev discount and tax (no user coupon applied).
     */
    public BigDecimal getFinalPriceWithTax() {
        BigDecimal afterDisc = getPriceAfterDeveloperDiscount();
        BigDecimal tax = computeTax(afterDisc);
        return afterDisc.add(tax);
    }
}