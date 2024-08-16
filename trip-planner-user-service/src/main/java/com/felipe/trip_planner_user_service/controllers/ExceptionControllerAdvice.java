package com.felipe.trip_planner_user_service.controllers;

import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_user_service.utils.response.ResponseConditionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(UserAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public CustomResponseBody<Void> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.CONFLICT);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }
}
