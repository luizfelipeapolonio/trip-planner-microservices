package com.felipe.trip_planner_trip_service.dtos.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record ActivityCreateOrUpdateDTO(
  @NotNull(message = "A descrição é obrigatória")
  @NotBlank(message = "A descrição não deve estar em branco")
  @Length(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
  String description
) {}
