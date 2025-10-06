package com.ashutosh.urban_cravin.controllers.users;

import com.ashutosh.urban_cravin.helpers.dtos.users.request.UserAddressRequest;
import com.ashutosh.urban_cravin.helpers.dtos.users.response.UserAddressResponse;
import com.ashutosh.urban_cravin.services.users.UserAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @PostMapping
    public UserAddressResponse addAddress(@PathVariable UUID userId,
                                          @Valid @RequestBody UserAddressRequest request) {
        return userAddressService.addAddress(userId, request);
    }

    @GetMapping
    public List<UserAddressResponse> getAddresses(@PathVariable UUID userId) {
        return userAddressService.getUserAddresses(userId);
    }

    @PutMapping("/{addressId}")
    public UserAddressResponse updateAddress(@PathVariable UUID userId,
                                             @PathVariable UUID addressId,
                                             @Valid @RequestBody UserAddressRequest request) {
        return userAddressService.updateAddress(addressId, request);
    }

    @DeleteMapping("/{addressId}")
    public void deleteAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        userAddressService.deleteAddress(addressId);
    }
}
