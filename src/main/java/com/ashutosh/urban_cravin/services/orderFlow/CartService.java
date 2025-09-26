package com.ashutosh.urban_cravin.services.orderFlow;

import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.models.orderFlow.CartItem;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.services.products.CouponService;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.repositories.UserRepo;
import com.ashutosh.urban_cravin.repositories.orderFlow.CartRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
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
    private CouponService couponService; // not used in addItem but available for future apply

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

        // check stock (optional)
        if (product.getStockQuantity() < qty) {
            throw new RuntimeException("Insufficient stock");
        }

        // check if item exists
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + qty);

        // Compute unit price:
        // 1) start with product price after developer discount
        BigDecimal unitBase = product.getPriceAfterDeveloperDiscount();

        // 2) tax is computed on amount after discounts (developer discount only here)
        BigDecimal taxPerUnit = product.computeTax(unitBase);

        // final per unit price (no user coupon applied in cart add)
        BigDecimal finalUnit = unitBase.add(taxPerUnit);

        item.setPrice(finalUnit.doubleValue()); // store final per unit price

        // compute cart total
        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(i -> i.getPrice() * i.getQuantity())
                        .sum()
        );

        return cartRepo.save(cart);
    }

    public Cart removeItem(UUID userId, UUID productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(i -> i.getPrice() * i.getQuantity())
                        .sum()
        );
        return cartRepo.save(cart);
    }
}
