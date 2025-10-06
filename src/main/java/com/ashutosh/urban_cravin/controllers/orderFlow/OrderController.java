package com.ashutosh.urban_cravin.controllers.orderFlow;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.dtos.orderFlow.CreateCheckoutRequest;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.users.UserPrincipal;
import com.ashutosh.urban_cravin.models.orderFlow.Order;
import com.ashutosh.urban_cravin.services.orderFlow.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CreateCheckoutRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        Order order = orderService.checkout(userId, req.getCouponCode());
        return ResponseEntity.ok(new ApiResponse(Status.Success, "Order created", Map.of("order", order)));
    }

}
