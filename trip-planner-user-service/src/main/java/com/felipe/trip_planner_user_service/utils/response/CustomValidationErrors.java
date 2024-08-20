package com.felipe.trip_planner_user_service.utils.response;

public class CustomValidationErrors {

  private String field;
  private Object rejectedValue;
  private String message;

  public CustomValidationErrors() {}

  public CustomValidationErrors(String field, Object rejectedValue, String message) {
    this.field = field;
    this.rejectedValue = rejectedValue;
    this.message = message;
  }

  public String getField() {
    return this.field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Object getRejectedValue() {
    return this.rejectedValue;
  }

  public void setRejectedValue(Object rejectedValue) {
    this.rejectedValue = rejectedValue;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
