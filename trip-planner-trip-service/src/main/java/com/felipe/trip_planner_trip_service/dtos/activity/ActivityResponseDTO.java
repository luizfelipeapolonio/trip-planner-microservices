package com.felipe.trip_planner_trip_service.dtos.activity;

import com.felipe.trip_planner_trip_service.models.Activity;

public record ActivityResponseDTO(String id, String description, String tripId, String ownerEmail, String createdAt, String updatedAt) {
  public ActivityResponseDTO(Activity activity) {
    this(
      activity.getId().toString(),
      activity.getDescription(),
      activity.getTrip().getId().toString(),
      activity.getOwnerEmail(),
      activity.getCreatedAt().toString(),
      activity.getUpdatedAt().toString()
    );
  }
}
