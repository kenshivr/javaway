package com.javaway.user.service;

import com.javaway.shared.util.SecurityUtils;
import com.javaway.user.dto.UpdateUserRequest;
import com.javaway.user.dto.UserResponse;
import com.javaway.user.mapper.UserMapper;
import com.javaway.user.model.User;
import com.javaway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    public UserResponse getProfile() {
        return userMapper.toResponse(securityUtils.getCurrentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateUserRequest request) {
        User user = securityUtils.getCurrentUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        return userMapper.toResponse(userRepository.save(user));
    }
}
