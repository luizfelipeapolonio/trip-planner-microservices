package com.felipe.trip_planner_mail_service.dtos;

import java.util.UUID;

public record TripInviteInfoDTO(UUID tripId, String destination, String ownerName, String ownerEmail, String startsAt, String endsAt) {
}
