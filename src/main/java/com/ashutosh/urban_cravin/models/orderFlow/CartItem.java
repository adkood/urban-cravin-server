package com.ashutosh.urban_cravin.models.orderFlow;

import com.ashutosh.urban_cravin.models.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, columnDefinition = "CHAR(36)")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private Product product;

    private Integer quantity;

    private Double unitPrice;   // per unit price (after discounts + tax)
    private Double totalPrice;  // unitPrice * quantity

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
