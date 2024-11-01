package com.felipe.trip_planner_trip_service.dtos.link;

import com.felipe.trip_planner_trip_service.models.Link;

public record LinkResponseDTO(
  String id,
  String title,
  String url,
  String tripId,
  String ownerEmail,
  String createdAt,
  String updatedAt
) {
  public LinkResponseDTO(Link link) {
    this(
      link.getId().toString(),
      link.getTitle(),
      link.getUrl(),
      link.getTrip().getId().toString(),
      link.getOwnerEmail(),
      link.getCreatedAt().toString(),
      link.getUpdatedAt().toString()
    );
  }
}
