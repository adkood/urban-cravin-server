package com.ashutosh.urban_cravin.helpers.dtos.orderFlow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartRequest {

    @NotNull(message = "Poduct Id is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    private int qty;

}
