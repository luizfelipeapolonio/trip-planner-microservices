package com.felipe.trip_planner_user_service.controllers;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_user_service.utils.response.CustomValidationErrors;
import com.felipe.trip_planner_user_service.utils.response.ResponseConditionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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

  @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public CustomResponseBody<Void> handleAuthenticationException(AuthenticationException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNAUTHORIZED);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public CustomResponseBody<List<CustomValidationErrors>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<CustomValidationErrors> errors = e.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(fieldError -> new CustomValidationErrors(
        fieldError.getField(),
        fieldError.getField().equalsIgnoreCase("password") ? "" : fieldError.getRejectedValue(),
        fieldError.getDefaultMessage()
      ))
      .toList();

    CustomResponseBody<List<CustomValidationErrors>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNPROCESSABLE_ENTITY);
    response.setMessage("Erros de validação");
    response.setData(errors);
    return response;
  }

  @ExceptionHandler(JWTCreationException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CustomResponseBody<Void> handleJWTCreationException(JWTCreationException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }
}
