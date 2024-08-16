package com.felipe.trip_planner_user_service.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("Usuário de e-mail '" + email + "' já cadastrado");
  }
}
