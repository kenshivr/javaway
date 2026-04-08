package com.javaway.user.mapper;

import com.javaway.user.dto.UserResponse;
import com.javaway.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
