package com.felipe.trip_planner_trip_service.exceptions;

public class InvalidInviteException extends RuntimeException {
  public InvalidInviteException() {
    super("Código de confirmação inválido");
  }
}
