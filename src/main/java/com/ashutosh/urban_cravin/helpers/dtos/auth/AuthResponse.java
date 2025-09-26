package com.ashutosh.urban_cravin.helpers.dtos.auth;

import com.ashutosh.urban_cravin.helpers.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private Status status;
    private String message;
    private String token;
}
