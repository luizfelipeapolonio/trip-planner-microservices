package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantDTO;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.repositories.ParticipantRepository;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

  @InjectMocks
  private ParticipantService participantService;

  @Mock
  private TripRepository tripRepository;

  @Mock
  private ParticipantRepository participantRepository;

  @Mock
  private InviteService inviteService;

  @Mock
  private InviteRepository inviteRepository;

  private Participant participant;
  private Invite invite;
  private Trip trip;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    trip.setDestination("Destino 1");
    trip.setOwnerName("User 1");
    trip.setOwnerEmail("user1@email.com");
    trip.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Invite invite = new Invite();
    invite.setCode(UUID.fromString("47875e77-5ab5-4386-b266-b8f589bace5a"));
    invite.setUserId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    invite.setUserEmail("user2@email.com");
    invite.setCreatedAt(mockDateTime);
    invite.setTrip(trip);

    Participant participant = new Participant();
    participant.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    participant.setName("User 2");
    participant.setEmail("user2@email.com");
    participant.setCreatedAt(mockDateTime);
    participant.setTrip(trip);

    this.participant = participant;
    this.invite = invite;
    this.trip = trip;
  }

  @Test
  @DisplayName("addParticipant - Should successfully add a participant to a trip")
  void addParticipantSuccess() {
    String userEmail = this.participant.getEmail();
    String userId = this.participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");

    when(this.inviteService.validateInvite(participantDTO.inviteCode(), userEmail, userId)).thenReturn(this.invite);
    when(this.tripRepository.findById(this.invite.getTrip().getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.save(any(Participant.class))).thenReturn(this.participant);

    Participant addedParticipant = this.participantService.addParticipant(participantDTO, userEmail, userId);

    assertThat(addedParticipant.getId()).isEqualTo(this.participant.getId());
    assertThat(addedParticipant.getName()).isEqualTo(this.participant.getName());
    assertThat(addedParticipant.getEmail()).isEqualTo(this.participant.getEmail());
    assertThat(addedParticipant.getTrip().getId()).isEqualTo(this.participant.getTrip().getId());
    assertThat(addedParticipant.getCreatedAt()).isEqualTo(this.participant.getCreatedAt());

    verify(this.inviteService, times(1)).validateInvite(participantDTO.inviteCode(), userEmail, userId);
    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).save(any(Participant.class));
    verify(this.inviteRepository, times(1)).delete(this.invite);
  }

  @Test
  @DisplayName("addParticipant - Should throw a RecordNotFoundException if trip is not found")
  void addParticipantFailsByTripNotFound() {
    String userEmail = this.participant.getEmail();
    String userId = this.participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");

    when(this.inviteService.validateInvite(participantDTO.inviteCode(), userEmail, userId)).thenReturn(this.invite);
    when(this.tripRepository.findById(this.invite.getTrip().getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.participantService.addParticipant(participantDTO, userEmail, userId));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Viagem de id: '%s' não encontrada", this.trip.getId());

    verify(this.inviteService, times(1)).validateInvite(participantDTO.inviteCode(), userEmail, userId);
    verify(this.tripRepository, times(1)).findById(this.invite.getTrip().getId());
    verify(this.participantRepository, never()).save(any(Participant.class));
    verify(this.inviteRepository, never()).delete(any(Invite.class));
  }
}

