package com.felipe.trip_planner_trip_service.dtos.trip;

import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.utils.response.ConvertDateFormat;

public record TripResponseDTO(
  String id,
  String destination,
  String ownerName,
  String ownerEmail,
  boolean isConfirmed,
  String startsAt,
  String endsAt,
  String createdAt,
  String updatedAt
) {
  public TripResponseDTO(Trip trip) {
    this(
      trip.getId().toString(),
      trip.getDestination(),
      trip.getOwnerName(),
      trip.getOwnerEmail(),
      trip.isConfirmed(),
      ConvertDateFormat.convertDateToFormattedString(trip.getStartsAt()),
      ConvertDateFormat.convertDateToFormattedString(trip.getEndsAt()),
      trip.getCreatedAt().toString(),
      trip.getUpdatedAt().toString()
    );
  }
}
