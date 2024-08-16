package com.felipe.trip_planner_user_service.services;

import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  UserService userService;

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  private User user;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    User user = new User();
    user.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    user.setName("User 1");
    user.setEmail("user1@email.com");
    user.setPassword("Encoded password");
    user.setCreatedAt(mockDateTime);
    user.setUpdatedAt(mockDateTime);

    this.user = user;
  }

  @Test
  @DisplayName("register - Should successfully create a user and return it")
  void registerUserSuccess() {
    UserRegisterDTO userRegisterDTO = new UserRegisterDTO("User 1", "user1@email.com", "password");

    when(this.userRepository.findByEmail(userRegisterDTO.email())).thenReturn(Optional.empty());
    when(this.passwordEncoder.encode(userRegisterDTO.password())).thenReturn("Enconded password");
    when(this.userRepository.save(any(User.class))).thenReturn(this.user);

    User createdUser = this.userService.register(userRegisterDTO);

    assertThat(createdUser.getId()).isEqualTo(this.user.getId());
    assertThat(createdUser.getName()).isEqualTo(this.user.getName());
    assertThat(createdUser.getEmail()).isEqualTo(this.user.getEmail());
    assertThat(createdUser.getPassword()).isEqualTo(this.user.getPassword());
    assertThat(createdUser.getCreatedAt()).isEqualTo(this.user.getCreatedAt());
    assertThat(createdUser.getUpdatedAt()).isEqualTo(this.user.getUpdatedAt());

    verify(this.userRepository, times(1)).findByEmail(userRegisterDTO.email());
    verify(this.passwordEncoder, times(1)).encode(userRegisterDTO.password());
    verify(this.userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("register - Should throw a UserAlreadyExistsException if user is already created")
  void registerUserFailsByExistingUser() {
    UserRegisterDTO userRegisterDTO = new UserRegisterDTO("User 1", "user1@email.com", "password");

    when(this.userRepository.findByEmail(userRegisterDTO.email())).thenReturn(Optional.of(this.user));

    Exception thrown = catchException(() -> this.userService.register(userRegisterDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(UserAlreadyExistsException.class)
      .hasMessage("Usuário de e-mail 'user1@email.com' já cadastrado");

    verify(this.userRepository, times(1)).findByEmail(userRegisterDTO.email());
    verify(this.passwordEncoder, never()).encode(anyString());
    verify(this.userRepository, never()).save(any(User.class));
  }
}
