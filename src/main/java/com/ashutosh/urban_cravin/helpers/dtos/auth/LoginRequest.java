package com.ashutosh.urban_cravin.helpers.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

}
