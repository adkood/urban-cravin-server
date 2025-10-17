package com.ashutosh.urban_cravin.controllers.users;

import com.ashutosh.urban_cravin.helpers.dtos.auth.response.AuthResponse;
import com.ashutosh.urban_cravin.helpers.dtos.auth.request.LoginRequest;
import com.ashutosh.urban_cravin.helpers.dtos.auth.request.SignupRequest;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.services.users.AuthService;
import com.ashutosh.urban_cravin.services.users.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = authService.login(req.getUsernameOrEmail(), req.getPassword());
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(Status.Success, "Login successful", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(Status.Error, e.getMessage(), null));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        try {
            User user = authService.register(req.getUsername(), req.getEmail(), req.getPassword());
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(Status.Success, "User registered successfully", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(Status.Error, e.getMessage(), null));
        }
    }
}
