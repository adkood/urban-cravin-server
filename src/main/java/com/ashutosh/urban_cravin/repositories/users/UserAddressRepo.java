package com.ashutosh.urban_cravin.repositories.users;

import com.ashutosh.urban_cravin.models.users.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAddressRepo extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByUserId(UUID userId);
}

