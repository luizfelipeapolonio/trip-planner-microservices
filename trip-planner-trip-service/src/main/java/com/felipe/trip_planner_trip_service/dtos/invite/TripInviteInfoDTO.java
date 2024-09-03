package com.felipe.trip_planner_trip_service.dtos.invite;

import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.utils.ConvertDateFormat;

import java.util.UUID;

public record TripInviteInfoDTO(UUID tripId, String destination, String ownerName, String ownerEmail, String startsAt, String endsAt) {
  public TripInviteInfoDTO(Trip trip) {
    this(
      trip.getId(),
      trip.getDestination(),
      trip.getOwnerName(),
      trip.getOwnerEmail(),
      ConvertDateFormat.convertDateToFormattedString(trip.getStartsAt()),
      ConvertDateFormat.convertDateToFormattedString(trip.getEndsAt())
    );
  }
}
