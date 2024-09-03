package com.felipe.trip_planner_trip_service.utils;

public enum Actions {
  GET("acessar"),
  DELETE("excluir"),
  UPDATE("alterar");

  private final String value;

  Actions(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
