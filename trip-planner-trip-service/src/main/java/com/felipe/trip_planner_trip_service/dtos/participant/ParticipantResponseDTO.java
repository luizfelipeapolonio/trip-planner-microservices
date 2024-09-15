package com.felipe.trip_planner_trip_service.dtos.participant;

import com.felipe.trip_planner_trip_service.models.Participant;

public record ParticipantResponseDTO(String id, String name, String email, String tripId, String createdAt) {
  public ParticipantResponseDTO(Participant participant) {
    this(
      participant.getId().toString(),
      participant.getName(),
      participant.getEmail(),
      participant.getTrip().getId().toString(),
      participant.getCreatedAt().toString()
    );
  }
}
