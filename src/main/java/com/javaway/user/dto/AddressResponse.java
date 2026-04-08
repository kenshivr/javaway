package com.javaway.user.dto;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        boolean isDefault
) {}
