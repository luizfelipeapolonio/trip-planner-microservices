package com.felipe.trip_planner_user_service.dtos;

import com.felipe.trip_planner_user_service.models.User;

import java.time.LocalDateTime;

public record UserResponseDTO(String id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
  public UserResponseDTO(User user) {
    this(user.getId().toString(), user.getName(), user.getEmail(), user.getCreatedAt(), user.getUpdatedAt());
  }
}
