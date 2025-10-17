package com.ashutosh.urban_cravin.repositories.payment;

import com.ashutosh.urban_cravin.models.payment.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface PaymentEventRepo extends JpaRepository<PaymentEvent, UUID> {
}