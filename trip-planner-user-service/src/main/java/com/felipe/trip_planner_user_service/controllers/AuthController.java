package com.felipe.trip_planner_user_service.controllers;

import com.felipe.trip_planner_user_service.dtos.UserLoginDTO;
import com.felipe.trip_planner_user_service.dtos.UserLoginResponseDTO;
import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.dtos.UserResponseDTO;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.services.UserService;
import com.felipe.trip_planner_user_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_user_service.utils.response.ResponseConditionStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<UserResponseDTO> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
    User createdUser = this.userService.register(userRegisterDTO);
    UserResponseDTO userResponseDTO = new UserResponseDTO(createdUser);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Usuário criado com sucesso");
    response.setData(userResponseDTO);
    return response;
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserLoginResponseDTO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
    Map<String, Object> login = this.userService.login(loginDTO);
    User user = (User) login.get("user");
    String token = (String) login.get("token");
    UserResponseDTO userInfo = new UserResponseDTO(user);
    UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO(userInfo, token);

    CustomResponseBody<UserLoginResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário logado com sucesso");
    response.setData(userLoginResponseDTO);
    return response;
  }
}
