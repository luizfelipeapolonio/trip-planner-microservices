package com.felipe.trip_planner_trip_service.exceptions;

public class InvalidDateException extends RuntimeException {
  public InvalidDateException(String message) {
    super(message);
  }
  public InvalidDateException(String message, Throwable cause) {
    super(message, cause);
  }
}
