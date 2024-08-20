package com.felipe.trip_planner_user_service.dtos;

public record UserLoginResponseDTO(UserResponseDTO userInfo, String token) {
}
