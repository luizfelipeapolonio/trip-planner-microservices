package com.felipe.trip_planner_trip_service.dtos.trip;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;

public record TripExtraInfoResponseDTO(
  ParticipantResponsePageDTO participantsList,
  ActivityResponsePageDTO activitiesList,
  LinkResponsePageDTO linksList
) {}
