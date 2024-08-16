package com.felipe.trip_planner_user_service.utils.response;

public enum ResponseConditionStatus {
  SUCCESS("Success"),
  ERROR("Error");

  private final String value;

  ResponseConditionStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
