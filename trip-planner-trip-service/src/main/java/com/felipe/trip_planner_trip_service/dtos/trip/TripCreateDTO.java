package com.felipe.trip_planner_trip_service.dtos.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TripCreateDTO(
  @NotNull(message = "O destino é obrigatório")
  @NotBlank(message = "O destino não deve ser nulo")
  String destination,

  @Valid
  @NotNull(message = "A data de início é obrigatória")
  TripDateDTO startsAt,

  @Valid
  @NotNull(message = "A data de término é obrigatória")
  TripDateDTO endsAt
) {}
