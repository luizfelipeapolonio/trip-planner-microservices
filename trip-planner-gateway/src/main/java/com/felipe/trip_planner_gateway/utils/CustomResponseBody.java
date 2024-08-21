package com.felipe.trip_planner_gateway.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResponseBody<T> {

  private String status;
  private int code;
  private String message;
  private T data;

  public CustomResponseBody() {}

  public CustomResponseBody(ResponseConditionStatus status, HttpStatus code, String message, T data) {
    this.status = status.getValue();
    this.code = code.value();
    this.message = message;
    this.data = data;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(ResponseConditionStatus status) {
    this.status = status.getValue();
  }

  public int getCode() {
    return this.code;
  }

  public void setCode(HttpStatus code) {
    this.code = code.value();
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return this.data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
