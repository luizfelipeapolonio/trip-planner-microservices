package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantDTO;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.ParticipantRepository;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.springframework.stereotype.Service;

@Service
public class ParticipantService {

  private final ParticipantRepository participantRepository;
  private final InviteService inviteService;
  private final TripRepository tripRepository;

  public ParticipantService(
    ParticipantRepository participantRepository,
    InviteService inviteService,
    TripRepository tripRepository
  ) {
    this.participantRepository = participantRepository;
    this.inviteService = inviteService;
    this.tripRepository = tripRepository;
  }

  public Participant addParticipant(AddParticipantDTO addParticipantDTO, String participantEmail, String participantId) {
    Invite validatedInvite = this.inviteService.validateInvite(addParticipantDTO.inviteCode(), participantEmail, participantId);
    Trip trip = this.tripRepository.findById(validatedInvite.getTrip().getId())
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + validatedInvite.getTrip().getId() + "' não encontrada"));

    Participant newParticipant = new Participant();
    newParticipant.setId(validatedInvite.getUserId());
    // TODO: adicionar o nome do participant na entity de Invite
    newParticipant.setName("IMPLEMENTAR");
    newParticipant.setEmail(validatedInvite.getUserEmail());
    newParticipant.setTrip(trip);

    return this.participantRepository.save(newParticipant);
  }
}
