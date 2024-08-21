package com.felipe.trip_planner_gateway.exceptions;

public class AuthValidationException extends RuntimeException {
  public AuthValidationException(String body) {
    super(body);
  }
}
