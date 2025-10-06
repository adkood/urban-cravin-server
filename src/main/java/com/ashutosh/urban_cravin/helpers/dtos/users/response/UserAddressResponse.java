package com.ashutosh.urban_cravin.helpers.dtos.users.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserAddressResponse {
    private UUID id;
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;
}
