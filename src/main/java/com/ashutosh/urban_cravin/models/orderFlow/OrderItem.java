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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private Product product;

    private Integer quantity;

    private Double unitPrice;
    private Double totalPrice;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
