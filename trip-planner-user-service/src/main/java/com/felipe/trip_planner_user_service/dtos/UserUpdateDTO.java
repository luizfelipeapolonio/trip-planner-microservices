package com.felipe.trip_planner_user_service.dtos;

import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.Length;

public record UserUpdateDTO(
  @Nullable
  @Length(max = 100, message = "O nome deve ter no máximo 100 caracteres")
  String name,

  @Nullable
  @Length(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
  String password
) {}
