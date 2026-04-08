package com.javaway.user.service;

import com.javaway.shared.util.SecurityUtils;
import com.javaway.user.dto.AddressRequest;
import com.javaway.user.dto.AddressResponse;
import com.javaway.user.mapper.AddressMapper;
import com.javaway.user.model.Address;
import com.javaway.user.model.User;
import com.javaway.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final SecurityUtils securityUtils;

    public List<AddressResponse> getAll() {
        Long userId = securityUtils.getCurrentUser().getId();
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User user = securityUtils.getCurrentUser();
        Address address = Address.builder()
                .user(user)
                .street(request.street())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .country(request.country())
                .isDefault(false)
                .build();
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setDefault(Long addressId) {
        Long userId = securityUtils.getCurrentUser().getId();

        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(current -> {
                    current.setDefault(false);
                    addressRepository.save(current);
                });

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        address.setDefault(true);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long addressId) {
        Long userId = securityUtils.getCurrentUser().getId();
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        addressRepository.delete(address);
    }
}
