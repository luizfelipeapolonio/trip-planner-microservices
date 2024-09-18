package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.repositories.ParticipantRepository;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParticipantService {

  private final ParticipantRepository participantRepository;
  private final InviteService inviteService;
  private final TripRepository tripRepository;
  private final InviteRepository inviteRepository;
  private final Logger logger = LoggerFactory.getLogger(ParticipantService.class);

  public ParticipantService(
    ParticipantRepository participantRepository,
    InviteService inviteService,
    TripRepository tripRepository,
    InviteRepository inviteRepository
  ) {
    this.participantRepository = participantRepository;
    this.inviteService = inviteService;
    this.tripRepository = tripRepository;
    this.inviteRepository = inviteRepository;
  }

  public Participant addParticipant(AddParticipantDTO addParticipantDTO, String participantEmail, String participantId) {
    Invite validatedInvite = this.inviteService.validateInvite(addParticipantDTO.inviteCode(), participantEmail, participantId);
    Trip trip = this.tripRepository.findById(validatedInvite.getTrip().getId())
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + validatedInvite.getTrip().getId() + "' não encontrada"));

    Participant newParticipant = new Participant();
    newParticipant.setId(validatedInvite.getUserId());
    newParticipant.setName(validatedInvite.getUsername());
    newParticipant.setEmail(validatedInvite.getUserEmail());
    newParticipant.setTrip(trip);

    Participant addedParticipant = this.participantRepository.save(newParticipant);
    logger.info(
      "Excluindo invite -> code: {} - email: {} - tripId: {}",
      validatedInvite.getCode(), validatedInvite.getUserEmail(), validatedInvite.getTrip().getId()
    );
    this.inviteRepository.delete(validatedInvite);
    return addedParticipant;
  }

  public Page<Participant> getAllTripParticipants(UUID tripId, String userEmail, int pageNumber) {
    Trip trip = this.tripRepository.findById(tripId)
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + tripId + "' não encontrada"));

    Pageable pagination = PageRequest.of(pageNumber, 10);
    Page<Participant> allParticipants = this.participantRepository.findAllByTripId(tripId, pagination);

    if(!trip.getOwnerEmail().equals(userEmail) && !isTripParticipant(allParticipants.getContent(), userEmail)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso");
    }
    return allParticipants;
  }

  public Participant removeParticipant(UUID tripId, UUID participantId, String userEmail) {
    Trip trip = this.tripRepository.findById(tripId)
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + tripId + "' não encontrada"));

    Participant participant = this.participantRepository.findByIdAndTripId(participantId, tripId)
      .orElseThrow(() -> new RecordNotFoundException("Participante de id: '" + participantId + "' não encontrado"));

    if(!trip.getOwnerEmail().equals(userEmail) && !participant.getEmail().equals(userEmail)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso");
    }
    this.participantRepository.deleteById(participant.getId());
    return participant;
  }

  private boolean isTripParticipant(List<Participant> participants, String userEmail) {
    Optional<Participant> tripParticipant = participants.stream()
      .filter(participant -> participant.getEmail().equals(userEmail))
      .findFirst();
    return tripParticipant.isPresent();
  }
}
