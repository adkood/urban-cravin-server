package com.ashutosh.urban_cravin.controllers.orderFlow;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.orderFlow.request.AddToCartRequest;
import com.ashutosh.urban_cravin.helpers.dtos.orderFlow.response.CartResponse;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.orderFlow.Cart;
import com.ashutosh.urban_cravin.models.users.UserPrincipal;
import com.ashutosh.urban_cravin.services.orderFlow.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        Cart cart = cartService.getCart(userId);
        CartResponse dto = CartResponse.mapToCartDto(cart);
        return ResponseEntity.ok(new ApiResponse(Status.Success, "User cart fetched successfully", Map.of("cart", dto)));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItem(@Valid @RequestBody AddToCartRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(cartService.addItem(userId, request.getProductId(), request.getQty()));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Cart> removeItem(@RequestParam UUID productId, @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }
}
