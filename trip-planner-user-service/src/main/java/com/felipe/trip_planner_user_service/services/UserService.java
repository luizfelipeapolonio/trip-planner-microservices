package com.felipe.trip_planner_user_service.services;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.felipe.trip_planner_user_service.dtos.UserLoginDTO;
import com.felipe.trip_planner_user_service.dtos.UserResponseDTO;
import com.felipe.trip_planner_user_service.dtos.UserUpdateDTO;
import com.felipe.trip_planner_user_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.repositories.UserRepository;
import com.felipe.trip_planner_user_service.security.AuthService;
import com.felipe.trip_planner_user_service.security.JwtService;
import com.felipe.trip_planner_user_service.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuthService authService;

  public UserService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager,
    JwtService jwtService,
    AuthService authService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.authService = authService;
  }

  public User register(UserRegisterDTO userRegisterDTO) {
    Optional<User> existingUser = this.userRepository.findByEmail(userRegisterDTO.email());

    if(existingUser.isPresent()) {
      throw new UserAlreadyExistsException(userRegisterDTO.email());
    }

    User newUser = new User();
    newUser.setName(userRegisterDTO.name());
    newUser.setEmail(userRegisterDTO.email());
    newUser.setPassword(this.passwordEncoder.encode(userRegisterDTO.password()));

    return this.userRepository.save(newUser);
  }

  public Map<String, Object> login(UserLoginDTO loginDTO) {
    try {
      var auth = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());
      Authentication authentication = this.authenticationManager.authenticate(auth);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String token = this.jwtService.generateToken(userPrincipal);

      Map<String, Object> loginResponse = new HashMap<>(2);
      loginResponse.put("user", userPrincipal.getUser());
      loginResponse.put("token", token);

      return loginResponse;
    } catch(BadCredentialsException e) {
      throw new BadCredentialsException("Usuário ou senha inválidos", e);
    }
  }

  public UserResponseDTO validateToken(String token) {
    String email = this.jwtService.validateToken(token);
    return this.userRepository.findByEmail(email)
      .map(UserResponseDTO::new)
      .orElseThrow(() -> new JWTVerificationException("Token inválido"));
  }

  public User getAuthenticatedUserProfile() {
    Authentication authentication = this.authService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return userPrincipal.getUser();
  }

  public User getProfile(String email) {
    return this.userRepository.findByEmail(email)
      .orElseThrow(() -> new RecordNotFoundException("Usuário de email: '" + email + "' não encontrado"));
  }

  public User update(UUID userId, UserUpdateDTO updateDTO) {
    Authentication authentication = this.authService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    UUID authenticatedUserId = userPrincipal.getUser().getId();

    if(!userId.equals(authenticatedUserId)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso");
    }

    return this.userRepository.findById(userId)
      .map(foundUser ->  {
        if(updateDTO.name() != null) {
          foundUser.setName(updateDTO.name());
        }
        if(updateDTO.password() != null) {
          foundUser.setPassword(this.passwordEncoder.encode(updateDTO.password()));
        }
        return this.userRepository.save(foundUser);
      })
      .orElseThrow(() -> new RecordNotFoundException("Usuário de id: '" + userId + "' não encontrado"));
  }

  public User deleteAuthenticatedUserProfile() {
    Authentication authentication = this.authService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    User authenticatedUser = userPrincipal.getUser();
    this.userRepository.deleteById(authenticatedUser.getId());
    return authenticatedUser;
  }
}
