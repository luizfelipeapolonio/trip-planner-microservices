package com.felipe.trip_planner_trip_service.dtos.invite;

import java.util.UUID;

public record CreatedInviteDTO(UUID inviteCode, TripInviteInfoDTO trip, ParticipantInviteInfoDTO participant) {
}
