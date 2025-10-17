package com.ashutosh.urban_cravin.repositories.payment;

import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import com.ashutosh.urban_cravin.models.payment.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepo extends JpaRepository<PaymentIntent, UUID> {
    Optional<PaymentIntent> findByMerchantTransactionId(String merchantTransactionId);

    List<PaymentIntent> findByStatusInAndCreatedAtBefore(
            List<PaymentIntentStatus> statuses,
            LocalDateTime createdAt
    );
}