package com.javaway.user.mapper;

import com.javaway.user.dto.AddressResponse;
import com.javaway.user.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "isDefault", expression = "java(address.isDefault())")
    AddressResponse toResponse(Address address);
}
