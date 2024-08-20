package com.felipe.trip_planner_user_service.services;

import com.felipe.trip_planner_user_service.dtos.UserLoginDTO;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.repositories.UserRepository;
import com.felipe.trip_planner_user_service.security.JwtService;
import com.felipe.trip_planner_user_service.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public UserService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager,
    JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
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
}
