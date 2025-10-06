package com.ashutosh.urban_cravin.helpers.dtos.users.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserAddressRequest {

//    @NotBlank(message = "Full name is required")
//    private String fullName;

    @NotBlank(message = "Street address is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Postal code must be 6 digits")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    private boolean isDefault = false;
}
