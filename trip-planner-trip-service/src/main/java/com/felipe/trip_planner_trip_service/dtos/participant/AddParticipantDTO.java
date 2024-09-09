package com.felipe.trip_planner_trip_service.dtos.participant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddParticipantDTO(
  @NotNull(message = "O código de confirmação é obrigatório")
  @NotBlank(message = "O código de confirmação não deve estar em branco")
  String inviteCode
) {}
