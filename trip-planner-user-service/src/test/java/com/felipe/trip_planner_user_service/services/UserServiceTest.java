package com.felipe.trip_planner_user_service.services;

import com.felipe.trip_planner_user_service.dtos.UserLoginDTO;
import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.repositories.UserRepository;
import com.felipe.trip_planner_user_service.security.JwtService;
import com.felipe.trip_planner_user_service.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
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

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  Authentication authentication;

  @Mock
  JwtService jwtService;

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

  @Test
  @DisplayName("login - Should successfully log the user in, generate access token, and return the user info and the token")
  void loginSuccess() {
    UserLoginDTO loginDTO = new UserLoginDTO("user1@email.com", "123456");
    var auth = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());
    UserPrincipal userPrincipal = new UserPrincipal(this.user);

    when(this.authenticationManager.authenticate(auth)).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.jwtService.generateToken(userPrincipal)).thenReturn("Access Token");

    Map<String, Object> loginResponse = this.userService.login(loginDTO);

    assertThat(loginResponse.containsKey("user")).isTrue();
    assertThat(loginResponse.containsKey("token")).isTrue();
    assertThat(loginResponse.get("user"))
      .extracting("id", "name", "email", "password", "createdAt", "updatedAt")
      .containsExactly(
        this.user.getId(),
        this.user.getName(),
        this.user.getEmail(),
        this.user.getPassword(),
        this.user.getCreatedAt(),
        this.user.getUpdatedAt()
      );
    assertThat(loginResponse.get("token")).isEqualTo("Access Token");

    verify(this.authenticationManager, times(1)).authenticate(auth);
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.jwtService, times(1)).generateToken(userPrincipal);
  }

  @Test
  @DisplayName("login - Should throw a BadCredentialsException if credentials are invalid")
  void loginFailsByBadCredentials() {
    UserLoginDTO loginDTO = new UserLoginDTO("user1@email.com", "123456");
    var auth = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());

    when(this.authenticationManager.authenticate(auth)).thenThrow(BadCredentialsException.class);

    Exception thrown = catchException(() -> this.userService.login(loginDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(BadCredentialsException.class)
      .hasMessage("Usuário ou senha inválidos");

    verify(this.authenticationManager, times(1)).authenticate(auth);
    verify(this.authentication, never()).getPrincipal();
    verify(this.jwtService, never()).generateToken(any(UserPrincipal.class));
  }
}
