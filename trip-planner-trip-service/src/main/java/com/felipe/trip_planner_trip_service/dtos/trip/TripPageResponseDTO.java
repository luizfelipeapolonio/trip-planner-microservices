package com.felipe.trip_planner_trip_service.dtos.trip;

import java.util.List;

public record TripPageResponseDTO(List<TripResponseDTO> trips, long totalElements, int totalPages) {
}
