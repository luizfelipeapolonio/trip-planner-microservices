package com.felipe.trip_planner_trip_service.dtos.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TripDateDTO(
  @NotNull(message = "O dia é obrigatório")
  @NotBlank(message = "O dia não deve ser nulo")
  @Pattern(regexp = "^\\d{2}", message = "Valor inválido! Use o formato de dois dígitos. Ex: 01")
  String day,

  @NotNull(message = "O mês é obrigatório")
  @NotBlank(message = "O mês não deve ser nulo")
  @Pattern(regexp = "^\\d{2}", message = "Valor inválido! Use o formato de dois dígitos. Ex: 01")
  String month,

  @NotNull(message = "O ano é obrigatório")
  @NotBlank(message = "O ano não deve ser nulo")
  @Pattern(regexp = "^\\d{4}", message = "Valor inválido! Use o formato de quatro dígitos. Ex: 2024")
  String year
) {}
