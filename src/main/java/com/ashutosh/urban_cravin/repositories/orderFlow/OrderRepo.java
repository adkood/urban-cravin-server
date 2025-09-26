package com.ashutosh.urban_cravin.repositories.orderFlow;

import com.ashutosh.urban_cravin.models.orderFlow.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepo extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
}
