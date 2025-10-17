package com.ashutosh.urban_cravin.services.orderFlow;

import com.ashutosh.urban_cravin.helpers.enums.OrderStatus;
import com.ashutosh.urban_cravin.models.orderFlow.*;
import com.ashutosh.urban_cravin.models.product.Coupon;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.repositories.orderFlow.CartRepo;
import com.ashutosh.urban_cravin.repositories.orderFlow.OrderRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import com.ashutosh.urban_cravin.services.products.CouponService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    // ----------------- PUBLIC METHODS -------------------

    @Transactional
    public Order checkout(UUID userId, String couponCode) {
        Cart cart = getValidCart(userId);

        Coupon coupon = (couponCode != null && !couponCode.isBlank())
                ? couponService.validateAndGet(couponCode)
                : null;

        Order order = createOrderFromCart(cart, coupon);

        clearCart(cart);

        Order savedOrder = orderRepo.save(order);

        if (coupon != null) {
            trackCouponUsage(coupon, savedOrder);
        }

        return savedOrder;
    }

    public Order checkout(UUID userId) {
        return checkout(userId, null);
    }

    // ----------------- PRIVATE HELPERS -------------------

    private Cart getValidCart(UUID userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }
        return cart;
    }

    private Order createOrderFromCart(Cart cart, Coupon coupon) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal orderTotal = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            Product product = fetchAndValidateProduct(ci);

            OrderItem orderItem = createOrderItem(ci, product, coupon, order);

            order.getItems().add(orderItem);
            orderTotal = orderTotal.add(orderItem.getTotalPrice());

            updateProductStock(product, ci.getQuantity());
        }

        order.setTotalPrice(orderTotal);  // BigDecimal now
        return order;
    }

    private Product fetchAndValidateProduct(CartItem ci) {
        Product product = productRepo.findById(ci.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + ci.getProduct().getId()));

        if (product.getStockQuantity() < ci.getQuantity()) {
            throw new OutOfStockException("Insufficient stock for product: " + product.getName());
        }
        return product;
    }

    private OrderItem createOrderItem(CartItem ci, Product product, Coupon coupon, Order order) {
        BigDecimal basePrice = product.getPriceAfterDeveloperDiscount();

        BigDecimal couponDiscount = (coupon != null)
                ? couponService.computeItemCouponDiscount(coupon, product, basePrice)
                : BigDecimal.ZERO;

        BigDecimal taxable = basePrice.subtract(couponDiscount).max(BigDecimal.ZERO);

        BigDecimal taxPerUnit = product.computeTax(taxable);
        BigDecimal finalUnitPrice = taxable.add(taxPerUnit);

        int qty = ci.getQuantity();
        BigDecimal totalPrice = finalUnitPrice.multiply(BigDecimal.valueOf(qty));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(qty);
        orderItem.setUnitPrice(finalUnitPrice); // BigDecimal
        orderItem.setTotalPrice(totalPrice);    // BigDecimal

        return orderItem;
    }

    private void updateProductStock(Product product, int qty) {
        product.setStockQuantity(product.getStockQuantity() - qty);
        productRepo.save(product);
    }

    private void clearCart(Cart cart) {
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO); // BigDecimal
        cartRepo.save(cart);
    }

    private void trackCouponUsage(Coupon coupon, Order order) {
        boolean applied = order.getItems().stream().anyMatch(oi -> {
            BigDecimal base = oi.getProduct().getPriceAfterDeveloperDiscount();
            BigDecimal discount = couponService.computeItemCouponDiscount(coupon, oi.getProduct(), base);
            return discount.compareTo(BigDecimal.ZERO) > 0;
        });

        if (applied) couponService.incrementUsage(coupon);
    }

    // ----------------- CUSTOM EXCEPTIONS -------------------

    public static class CartNotFoundException extends RuntimeException {
        public CartNotFoundException(String message) { super(message); }
    }

    public static class EmptyCartException extends RuntimeException {
        public EmptyCartException(String message) { super(message); }
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) { super(message); }
    }

    public static class OutOfStockException extends RuntimeException {
        public OutOfStockException(String message) { super(message); }
    }
}
