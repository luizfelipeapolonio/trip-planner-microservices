package com.felipe.trip_planner_trip_service.dtos.participant;

import com.felipe.trip_planner_trip_service.models.Participant;

public record ParticipantResponseInfoDTO(String id, String name, String email) {
  public ParticipantResponseInfoDTO(Participant participant) {
    this(participant.getId().toString(), participant.getName(), participant.getEmail());
  }
}
