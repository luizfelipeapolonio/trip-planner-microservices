package com.felipe.trip_planner_mail_service.dtos;

import java.util.UUID;

public record CreatedInviteDTO(UUID inviteCode, TripInviteInfoDTO trip, ParticipantInviteInfoDTO participant) {
}
