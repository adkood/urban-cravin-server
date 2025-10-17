package com.ashutosh.urban_cravin.models.payment;

import com.ashutosh.urban_cravin.helpers.enums.PaymentEventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_events")
public class PaymentEvent {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @NotNull(message = "PaymentIntent ID must not be null")
    @Column(nullable = false)
    private UUID paymentIntentId;

    @NotNull(message = "User ID is required to associate the event")
    @Column(nullable = false)
    private UUID userId;

    @NotBlank(message = "Event type must be specified")
    @Enumerated(EnumType.STRING)
    private PaymentEventStatus eventType;

    @Lob
    private String payload;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
