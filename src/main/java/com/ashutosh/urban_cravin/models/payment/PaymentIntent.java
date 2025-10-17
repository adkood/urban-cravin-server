// PaymentIntent.java - Updated
package com.ashutosh.urban_cravin.models.payment;

import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "payment_intents",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"merchantTransactionId"})
        }
)
public class PaymentIntent {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @NotBlank(message = "Merchant transaction ID must not be blank")
    @Column(nullable = false, unique = true)
    private String merchantTransactionId;

    @NotNull(message = "User ID is required for tracking payment ownership")
    @Column(nullable = false)
    private UUID userId;

    private UUID orderId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.1", message = "Payment amount must be greater than 0")
    private BigDecimal amount; // in paise

    @NotBlank(message = "Currency code must not be blank")
    private String currency = "INR";

    private String transactionId; // returned by PhonePe when available

    @NotNull(message = "Payment status must not be blank")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentIntentStatus status = PaymentIntentStatus.PENDING;

    @Lob
    private String requestPayload;

    @Lob
    private String lastResponse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}