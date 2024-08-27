package com.felipe.trip_planner_trip_service.dtos.trip;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

public record TripUpdateDTO(
  @Nullable
  String destination,

  @Valid
  @Nullable
  TripDateDTO startsAt,

  @Valid
  @Nullable
  TripDateDTO endsAt
) {
}
