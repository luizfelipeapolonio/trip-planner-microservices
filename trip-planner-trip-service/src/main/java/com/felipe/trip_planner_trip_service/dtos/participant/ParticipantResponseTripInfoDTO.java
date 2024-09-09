package com.felipe.trip_planner_trip_service.dtos.participant;

import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.utils.ConvertDateFormat;

public record ParticipantResponseTripInfoDTO(String id, String destination, String startsAt, String endsAt) {
  public ParticipantResponseTripInfoDTO(Trip trip) {
    this(
      trip.getId().toString(),
      trip.getDestination(),
      ConvertDateFormat.convertDateToFormattedString(trip.getStartsAt()),
      ConvertDateFormat.convertDateToFormattedString(trip.getEndsAt())
    );
  }
}
