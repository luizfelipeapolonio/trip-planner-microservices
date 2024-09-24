package com.felipe.trip_planner_trip_service.dtos.activity;

import java.util.List;

public record ActivityResponsePageDTO(List<ActivityResponseDTO> activities, long totalElements, int totalPages) {
}
