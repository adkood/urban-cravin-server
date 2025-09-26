package com.ashutosh.urban_cravin.controllers.orderFlow;

import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.services.orderFlow.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Cart> addItem(
            @PathVariable UUID userId,
            @RequestParam UUID productId,
            @RequestParam int qty) {
        return ResponseEntity.ok(cartService.addItem(userId, productId, qty));
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<Cart> removeItem(
            @PathVariable UUID userId,
            @RequestParam UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }
}
