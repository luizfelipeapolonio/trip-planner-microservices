package com.felipe.trip_planner_trip_service.dtos.trip;

import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;

public record TripFullResponseDTO(TripResponseDTO trip, ParticipantResponsePageDTO participants) {
}
