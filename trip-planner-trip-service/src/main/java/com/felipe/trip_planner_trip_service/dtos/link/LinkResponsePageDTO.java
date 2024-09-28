package com.felipe.trip_planner_trip_service.dtos.link;

import java.util.List;

public record LinkResponsePageDTO(List<LinkResponseDTO> links, long totalElements, int totalPages) {
}
