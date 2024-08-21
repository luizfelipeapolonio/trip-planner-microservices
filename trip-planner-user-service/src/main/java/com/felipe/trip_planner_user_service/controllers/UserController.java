package com.felipe.trip_planner_user_service.controllers;

import com.felipe.trip_planner_user_service.dtos.UserResponseDTO;
import com.felipe.trip_planner_user_service.dtos.UserUpdateDTO;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.services.UserService;
import com.felipe.trip_planner_user_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_user_service.utils.response.ResponseConditionStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> getAuthenticatedUserProfile() {
    User authenticatedUser = this.userService.getAuthenticatedUserProfile();
    UserResponseDTO userResponseDTO = new UserResponseDTO(authenticatedUser);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário autenticado");
    response.setData(userResponseDTO);
    return response;
  }

  @PutMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> update(@PathVariable UUID userId, @RequestBody @Valid UserUpdateDTO updateDTO) {
    User updatedUser = this.userService.update(userId, updateDTO);
    UserResponseDTO userResponseDTO = new UserResponseDTO(updatedUser);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário atualizado com sucesso");
    response.setData(userResponseDTO);
    return response;
  }
}
