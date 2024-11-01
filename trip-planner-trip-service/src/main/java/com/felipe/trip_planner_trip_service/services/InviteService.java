package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.clients.UserClient;
import com.felipe.trip_planner_trip_service.dtos.UserClientDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.CreatedInviteDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.ParticipantInviteInfoDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.TripInviteInfoDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidInviteException;
import com.felipe.trip_planner_trip_service.exceptions.ParticipantAlreadyExistsException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.utils.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class InviteService {

  private final InviteRepository inviteRepository;
  private final TripService tripService;
  private final UserClient userClient;
  private final KafkaTemplate<String, CreatedInviteDTO> kafkaTemplate;
  private final Logger logger = LoggerFactory.getLogger(InviteService.class);

  public InviteService(
    InviteRepository inviteRepository,
    TripService tripService,
    UserClient userClient,
    KafkaTemplate<String, CreatedInviteDTO> kafkaTemplate
  ) {
    this.inviteRepository = inviteRepository;
    this.tripService = tripService;
    this.userClient = userClient;
    this.kafkaTemplate = kafkaTemplate;
  }

  public String invite(UUID tripId, String ownerEmail, InviteParticipantDTO inviteDTO) {
    Trip trip = this.tripService.checkIfIsTripOwner(tripId, ownerEmail, Actions.GET);
    UserClientDTO userClientDTO = this.userClient.getProfile(inviteDTO.email());
    this.checkForExistingParticipant(trip, inviteDTO.email());
    this.checkForExistingInviteAndDeleteIt(inviteDTO.email(), tripId);

    Invite newInvite = new Invite();
    newInvite.setTrip(trip);
    newInvite.setUsername(userClientDTO.name());
    newInvite.setUserEmail(userClientDTO.email());

    Invite createdInvite = this.inviteRepository.save(newInvite);

    CreatedInviteDTO createdInviteDTO = new CreatedInviteDTO(
      createdInvite.getCode(),
      new TripInviteInfoDTO(trip),
      new ParticipantInviteInfoDTO(createdInvite.getUsername(), createdInvite.getUserEmail())
    );

    logger.info(
      "Postando no tópico \"invite\" -> tripId: {} - userEmail: {}",
      createdInviteDTO.trip().tripId(), createdInviteDTO.participant().email()
    );
    this.kafkaTemplate.send("invite", createdInviteDTO);
    return createdInvite.getUserEmail();
  }

  public Invite validateInvite(String inviteCode, String userEmail) {
    UUID convertedInviteCode = UUID.fromString(inviteCode);
    Optional<Invite> invite = this.inviteRepository.findByCodeAndIsValidTrue(convertedInviteCode);

    if(invite.isEmpty() || !invite.get().getUserEmail().equals(userEmail)) {
      throw new InvalidInviteException();
    }
    return invite.get();
  }

  private void checkForExistingInviteAndDeleteIt(String userEmail, UUID tripId) {
    this.inviteRepository.findByUserEmailAndTripIdAndIsValidTrue(userEmail, tripId)
      .ifPresent(invite -> {
        logger.info(
          "Excluindo invite existente. userEmail: {} -  tripId: {}", invite.getUserEmail(), tripId
        );
        this.inviteRepository.delete(invite);
      });
  }

  private void checkForExistingParticipant(Trip trip, String userEmail) {
    Optional<Participant> existingParticipant = trip.getParticipants()
      .stream()
      .filter(participant -> participant.getEmail().equals(userEmail))
      .findFirst();

    if(existingParticipant.isPresent()) {
      throw new ParticipantAlreadyExistsException(existingParticipant.get().getEmail());
    }
  }
}
