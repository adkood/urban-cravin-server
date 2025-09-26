package com.ashutosh.urban_cravin.services.orderFlow;

//import com.ashutosh.urban_cravin.configs.RabbitMQConfig;
import com.ashutosh.urban_cravin.helpers.enums.OrderStatus;
import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.models.orderFlow.CartItem;
import com.ashutosh.urban_cravin.models.orderFlow.Order;
import com.ashutosh.urban_cravin.models.orderFlow.OrderItem;
import com.ashutosh.urban_cravin.models.product.Coupon;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.repositories.orderFlow.CartRepo;
import com.ashutosh.urban_cravin.repositories.orderFlow.OrderRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import com.ashutosh.urban_cravin.services.products.CouponService;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CouponService couponService;

    @Transactional
    public Order checkout(UUID userId, String couponCode) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Coupon coupon;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponService.validateAndGet(couponCode);
        } else {
            coupon = null;
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal orderTotal = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            Product p = productRepo.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 🔹 Check stock first
            if (p.getStockQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + p.getName());
            }

            // base = product price after developer discount
            BigDecimal unitBase = p.getPriceAfterDeveloperDiscount();

            // coupon discount per unit
            BigDecimal couponDiscountPerUnit = BigDecimal.ZERO;
            if (coupon != null) {
                couponDiscountPerUnit = couponService.computeItemCouponDiscount(coupon, p, unitBase);
            }

            BigDecimal taxable = unitBase.subtract(couponDiscountPerUnit);
            if (taxable.compareTo(BigDecimal.ZERO) < 0) taxable = BigDecimal.ZERO;

            BigDecimal taxPerUnit = p.computeTax(taxable);

            BigDecimal finalUnitPrice = taxable.add(taxPerUnit);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(finalUnitPrice.doubleValue());
            order.getItems().add(oi);

            // update order total
            orderTotal = orderTotal.add(finalUnitPrice.multiply(BigDecimal.valueOf(ci.getQuantity())));

            // 🔹 Deduct stock (inside the same transaction)
            p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());
            productRepo.save(p);
        }

        order.setTotalPrice(orderTotal.doubleValue());

        // clear cart
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);

        Order savedOrder = orderRepo.save(order);

        // increment coupon usage if applied
        if (coupon != null) {
            boolean applied = savedOrder.getItems().stream().anyMatch(oi -> {
                BigDecimal base = oi.getProduct().getPriceAfterDeveloperDiscount();
                BigDecimal d = couponService.computeItemCouponDiscount(coupon, oi.getProduct(), base);
                return d.compareTo(BigDecimal.ZERO) > 0;
            });

            if (applied) couponService.incrementUsage(coupon);
        }

        return savedOrder;
    }

    public Order checkout(UUID userId) {
        return checkout(userId, null);
    }
}
