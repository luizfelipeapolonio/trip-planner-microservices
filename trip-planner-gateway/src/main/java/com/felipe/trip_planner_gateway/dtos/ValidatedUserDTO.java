package com.felipe.trip_planner_gateway.dtos;

import java.time.LocalDateTime;

public record ValidatedUserDTO(String id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
