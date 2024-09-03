package com.felipe.trip_planner_trip_service.dtos;

import java.time.LocalDateTime;

public record UserClientDTO(String id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
