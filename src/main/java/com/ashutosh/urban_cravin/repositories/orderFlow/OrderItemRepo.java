package com.ashutosh.urban_cravin.repositories.orderFlow;

import com.ashutosh.urban_cravin.models.orderFlow.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, UUID> {
}
