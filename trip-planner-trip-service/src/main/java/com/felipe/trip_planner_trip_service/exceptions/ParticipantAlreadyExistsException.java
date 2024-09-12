package com.felipe.trip_planner_trip_service.exceptions;

public class ParticipantAlreadyExistsException extends RuntimeException {
  public ParticipantAlreadyExistsException(String email) {
    super(String.format("Usuário de e-mail '%s' já é um participante da viagem", email));
  }
}
