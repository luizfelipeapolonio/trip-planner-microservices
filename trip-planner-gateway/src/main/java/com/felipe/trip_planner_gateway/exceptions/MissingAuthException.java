package com.felipe.trip_planner_gateway.exceptions;

public class MissingAuthException extends RuntimeException {
  public MissingAuthException() {
    super("Autenticação é necessária para acessar este recurso");
  }
}
