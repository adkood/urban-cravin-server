package com.ashutosh.urban_cravin.services.orderFlow;

import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.models.orderFlow.CartItem;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.repositories.users.UserRepo;
import com.ashutosh.urban_cravin.repositories.orderFlow.CartRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import com.ashutosh.urban_cravin.services.products.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CouponService couponService;

    public Cart getCart(UUID userId) {
        return cartRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepo.save(cart);
                });
    }

    public Cart addItem(UUID userId, UUID productId, int qty) {
        if (qty <= 0) throw new RuntimeException("Quantity must be > 0");

        Cart cart = getCart(userId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < qty) {
            throw new RuntimeException("Insufficient stock");
        }

        // Find existing or create new
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    newItem.setUnitPrice(0.0);
                    newItem.setTotalPrice(0.0);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        // Update quantity
        item.setQuantity(item.getQuantity() + qty);

        // Calculate unit price
        BigDecimal unitBase = product.getPriceAfterDeveloperDiscount();
        BigDecimal taxPerUnit = product.computeTax(unitBase);
        BigDecimal finalUnit = unitBase.add(taxPerUnit);

        item.setUnitPrice(finalUnit.doubleValue());
        item.setTotalPrice(finalUnit.multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue());

        // Update cart total
        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getTotalPrice)
                        .sum()
        );

        return cartRepo.save(cart);
    }

    public Cart removeItem(UUID userId, UUID productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getTotalPrice)
                        .sum()
        );
        return cartRepo.save(cart);
    }
}
