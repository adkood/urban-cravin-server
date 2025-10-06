package com.ashutosh.urban_cravin.controllers.orderFlow;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.orderFlow.AddToCartRequest;
import com.ashutosh.urban_cravin.helpers.dtos.orderFlow.response.CartResponse;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.repositories.orderFlow.CartRepo;
import com.ashutosh.urban_cravin.services.orderFlow.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable UUID userId) {
        Cart cart = cartService.getCart(userId);
        CartResponse dto = CartResponse.mapToCartDto(cart);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "User cart fetched successfully", Map.of("cart", dto)));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Cart> addItem(
            @PathVariable UUID userId, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request.getProductId(), request.getQty()));
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<Cart> removeItem(
            @PathVariable UUID userId,
            @RequestParam UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }
}
