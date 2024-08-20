package com.felipe.trip_planner_user_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginDTO(
  @NotNull(message = "O e-mail é obrigatório")
  @NotBlank(message = "O e-mail não deve estar em branco")
  String email,

  @NotNull(message = "A senha é obrigatória")
  @NotBlank(message = "A senha não deve estar em branco")
  String password
) {}
