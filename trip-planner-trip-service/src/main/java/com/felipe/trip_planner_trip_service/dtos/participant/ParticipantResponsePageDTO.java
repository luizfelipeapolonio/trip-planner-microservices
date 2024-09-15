package com.felipe.trip_planner_trip_service.dtos.participant;

import java.util.List;

public record ParticipantResponsePageDTO(List<ParticipantResponseDTO> participants, long totalElements, int totalPages) {
}
