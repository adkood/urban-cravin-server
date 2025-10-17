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
import java.util.Iterator;
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
                    cart.setTotalPrice(BigDecimal.ZERO);
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

        // Find existing item or create new
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    newItem.setUnitPrice(BigDecimal.ZERO);
                    newItem.setTotalPrice(BigDecimal.ZERO);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        // Update quantity
        item.setQuantity(item.getQuantity() + qty);

        // Calculate unit price
        BigDecimal unitBase = product.getPriceAfterDeveloperDiscount(); // already BigDecimal
        BigDecimal taxPerUnit = product.computeTax(unitBase);           // BigDecimal
        BigDecimal finalUnit = unitBase.add(taxPerUnit);

        item.setUnitPrice(finalUnit);
        item.setTotalPrice(finalUnit.multiply(BigDecimal.valueOf(item.getQuantity())));

        // Update cart total
        BigDecimal cartTotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(cartTotal);

        return cartRepo.save(cart);
    }

    public Cart removeItem(UUID userId, UUID productId) {
        Cart cart = getCart(userId);

        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(productId)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);

                    // Recalculate totalPrice for this item
                    BigDecimal unitPrice = item.getUnitPrice();
                    item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                } else {
                    iterator.remove(); // safely remove item
                }
                break;
            }
        }

        // Recalculate cart total (BigDecimal)
        BigDecimal cartTotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(cartTotal);

        return cartRepo.save(cart);
    }
}
