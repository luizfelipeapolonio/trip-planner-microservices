package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.clients.UserClient;
import com.felipe.trip_planner_trip_service.dtos.UserClientDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.CreatedInviteDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.ParticipantInviteInfoDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.TripInviteInfoDTO;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.utils.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    Invite newInvite = new Invite();
    newInvite.setTrip(trip);
    newInvite.setUserId(UUID.fromString(userClientDTO.id()));
    newInvite.setUserEmail(userClientDTO.email());

    Invite createdInvite = this.inviteRepository.save(newInvite);

    CreatedInviteDTO createdInviteDTO = new CreatedInviteDTO(
      createdInvite.getCode(),
      new TripInviteInfoDTO(trip),
      new ParticipantInviteInfoDTO(userClientDTO.name(), userClientDTO.email())
    );

    logger.info(
      "Postando no tópico \"invite\" -> tripId: {} - userEmail: {}",
      createdInviteDTO.trip().tripId(), createdInviteDTO.participant().email()
    );
    this.kafkaTemplate.send("invite", createdInviteDTO);
    return createdInvite.getUserEmail();
  }
}