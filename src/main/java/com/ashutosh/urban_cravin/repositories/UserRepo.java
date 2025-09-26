package com.ashutosh.urban_cravin.repositories;

import com.ashutosh.urban_cravin.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    public User findByUsername(String username);

    public User findByUsernameOrEmail(String username, String email);

}
