package com.javaway.auth.service;

import com.javaway.auth.dto.AuthResponse;
import com.javaway.auth.dto.LoginRequest;
import com.javaway.auth.dto.RefreshRequest;
import com.javaway.auth.dto.RegisterRequest;
import com.javaway.auth.model.RefreshToken;
import com.javaway.shared.enums.Role;
import com.javaway.shared.security.JwtService;
import com.javaway.user.model.User;
import com.javaway.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password123", "John", "Doe", null);
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenService.generate(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any());
    }

    @Test
    void register_emailAlreadyInUse_throwsException() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password123", "John", "Doe", null);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@email.com", "password123");
        User user = User.builder().email(request.email()).role(Role.CUSTOMER).build();
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.generate(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throwsException() {
        LoginRequest request = new LoginRequest("test@email.com", "wrong-password");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        User user = User.builder().email("test@email.com").role(Role.CUSTOMER).build();
        RefreshToken existingToken = RefreshToken.builder().token("old-refresh").user(user).build();
        RefreshToken newToken = RefreshToken.builder().token("new-refresh").build();

        when(refreshTokenService.validate("old-refresh")).thenReturn(existingToken);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.generate(user)).thenReturn(newToken);

        AuthResponse response = authService.refresh(new RefreshRequest("old-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_invalidToken_throwsException() {
        when(refreshTokenService.validate("invalid")).thenThrow(new IllegalArgumentException("Invalid refresh token"));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("invalid")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");
    }
}
