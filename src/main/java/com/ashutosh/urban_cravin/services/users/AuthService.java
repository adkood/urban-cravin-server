package com.ashutosh.urban_cravin.services.users;

import com.ashutosh.urban_cravin.helpers.enums.Role;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.repositories.users.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public User login(String usernameOrEmail, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid username/email or password");
        }

        return userRepo.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    public User register(String username, String email, String password) {
        if (username == null || email == null || password == null) {
            throw new RuntimeException("username, email and password are required");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(password));
        newUser.setRole(Role.ROLE_USER);

        return userRepo.save(newUser);
    }
}
