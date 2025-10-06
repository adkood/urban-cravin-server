package com.ashutosh.urban_cravin.services.users;

import com.ashutosh.urban_cravin.helpers.dtos.users.request.UserAddressRequest;
import com.ashutosh.urban_cravin.helpers.dtos.users.response.UserAddressResponse;
import com.ashutosh.urban_cravin.models.users.User;
import com.ashutosh.urban_cravin.models.users.UserAddress;
import com.ashutosh.urban_cravin.repositories.users.UserAddressRepo;
import com.ashutosh.urban_cravin.repositories.users.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserAddressService {

    @Autowired
    private UserAddressRepo userAddressRepository;

    @Autowired
    private UserRepo userRepository;

    private UserAddressResponse mapToResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
//                .fullName(address.getFullName())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }

    public UserAddressResponse addAddress(UUID userId, UserAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAddress address = new UserAddress();
//        address.setFullName(request.getFullName());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());
        address.setUser(user);

        UserAddress saved = userAddressRepository.save(address);
        return mapToResponse(saved);
    }

    public List<UserAddressResponse> getUserAddresses(UUID userId) {
        return userAddressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserAddressResponse updateAddress(UUID addressId, UserAddressRequest request) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

//        address.setFullName(request.getFullName());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        UserAddress updated = userAddressRepository.save(address);
        return mapToResponse(updated);
    }

    public void deleteAddress(UUID addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        userAddressRepository.delete(address);
    }
}
