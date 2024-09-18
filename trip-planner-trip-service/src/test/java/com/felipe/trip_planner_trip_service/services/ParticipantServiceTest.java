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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

  private List<Participant> participants;
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

    Participant participant2 = new Participant();
    participant2.setId(UUID.fromString("b610a230-e186-4913-b260-c136f357c75d"));
    participant2.setName("User 3");
    participant2.setEmail("user3@email.com");
    participant2.setCreatedAt(mockDateTime);
    participant2.setTrip(trip);

    this.participants = List.of(participant, participant2);
    this.invite = invite;
    this.trip = trip;
  }

  @Test
  @DisplayName("addParticipant - Should successfully add a participant to a trip")
  void addParticipantSuccess() {
    Participant participant = this.participants.get(0);
    String userEmail = participant.getEmail();
    String userId = participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");

    when(this.inviteService.validateInvite(participantDTO.inviteCode(), userEmail, userId)).thenReturn(this.invite);
    when(this.tripRepository.findById(this.invite.getTrip().getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.save(any(Participant.class))).thenReturn(participant);

    Participant addedParticipant = this.participantService.addParticipant(participantDTO, userEmail, userId);

    assertThat(addedParticipant.getId()).isEqualTo(participant.getId());
    assertThat(addedParticipant.getName()).isEqualTo(participant.getName());
    assertThat(addedParticipant.getEmail()).isEqualTo(participant.getEmail());
    assertThat(addedParticipant.getTrip().getId()).isEqualTo(participant.getTrip().getId());
    assertThat(addedParticipant.getCreatedAt()).isEqualTo(participant.getCreatedAt());

    verify(this.inviteService, times(1)).validateInvite(participantDTO.inviteCode(), userEmail, userId);
    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).save(any(Participant.class));
    verify(this.inviteRepository, times(1)).delete(this.invite);
  }

  @Test
  @DisplayName("addParticipant - Should throw a RecordNotFoundException if trip is not found")
  void addParticipantFailsByTripNotFound() {
    Participant participant = this.participants.get(0);
    String userEmail = participant.getEmail();
    String userId = participant.getId().toString();
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

  @Test
  @DisplayName("getAllTripParticipants - The trip owner should successfully get all participants of a trip and return a Page with them")
  void getAllTripParticipantsBeingTripOwnerSuccess() {
    UUID tripId = this.trip.getId();
    String userEmail = "user1@email.com";

    PageImpl<Participant> participantsPage = new PageImpl<>(this.participants);
    Pageable pagination = PageRequest.of(0, 10);

    when(this.tripRepository.findById(tripId)).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findAllByTripId(tripId, pagination)).thenReturn(participantsPage);

    Page<Participant> returnedParticipantsPage = this.participantService.getAllTripParticipants(tripId, userEmail, 0);

    assertThat(returnedParticipantsPage.getTotalElements()).isEqualTo(participantsPage.getTotalElements());
    assertThat(returnedParticipantsPage.getContent())
      .allSatisfy(participant -> assertThat(participant.getTrip().getId()).isEqualTo(tripId));
    assertThat(returnedParticipantsPage.getContent().stream().map(Participant::getEmail).toList())
      .containsExactlyInAnyOrderElementsOf(participantsPage.getContent().stream().map(Participant::getEmail).toList());

    verify(this.tripRepository, times(1)).findById(tripId);
    verify(this.participantRepository, times(1)).findAllByTripId(tripId, pagination);
  }

  @Test
  @DisplayName("getAllTripParticipants - A trip participant should successfully get all participants of a trip and return a Page with them")
  void getAllTripParticipantsBeingParticipantSuccess() {
    UUID tripId = this.trip.getId();
    String userEmail = "user2@email.com";

    PageImpl<Participant> participantsPage = new PageImpl<>(this.participants);
    Pageable pagination = PageRequest.of(0, 10);

    when(this.tripRepository.findById(tripId)).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findAllByTripId(tripId, pagination)).thenReturn(participantsPage);

    Page<Participant> returnedParticipantsPage = this.participantService.getAllTripParticipants(tripId, userEmail, 0);

    assertThat(returnedParticipantsPage.getTotalElements()).isEqualTo(participantsPage.getTotalElements());
    assertThat(returnedParticipantsPage.getContent())
      .allSatisfy(participant -> assertThat(participant.getTrip().getId()).isEqualTo(tripId));
    assertThat(returnedParticipantsPage.getContent().stream().map(Participant::getEmail).toList())
      .containsExactlyInAnyOrderElementsOf(participantsPage.getContent().stream().map(Participant::getEmail).toList());

    verify(this.tripRepository, times(1)).findById(tripId);
    verify(this.participantRepository, times(1)).findAllByTripId(tripId, pagination);
  }

  @Test
  @DisplayName("getAllTripParticipants - Should throw an AccessDeniedException if the is not the trip owner or trip participant")
  void getAllTripParticipantsFailsByAccessDenied() {
    UUID tripId = this.trip.getId();
    String userEmail = "user4@email.com";

    PageImpl<Participant> participantsPage = new PageImpl<>(this.participants);
    Pageable pagination = PageRequest.of(0, 10);

    when(this.tripRepository.findById(tripId)).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findAllByTripId(tripId, pagination)).thenReturn(participantsPage);

    Exception thrown = catchException(() -> this.participantService.getAllTripParticipants(tripId, userEmail, 0));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para acessar este recurso");

    verify(this.tripRepository, times(1)).findById(tripId);
    verify(this.participantRepository, times(1)).findAllByTripId(tripId, pagination);
  }

  @Test
  @DisplayName("getAllTripParticipants - Should throw a RecordNotFoundException if the trip is not found")
  void getAllTripParticipantsFailsByTripNotFound() {
    UUID tripId = this.trip.getId();
    String userEmail = "user1@email.com";

    when(this.tripRepository.findById(tripId)).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.participantService.getAllTripParticipants(tripId, userEmail, 0));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Viagem de id: '%s' não encontrada", tripId);

    verify(this.tripRepository, times(1)).findById(tripId);
    verify(this.participantRepository, never()).findAllByTripId(any(UUID.class), any(Pageable.class));
  }

  @Test
  @DisplayName("removeParticipant - Should successfully remove a participant as the trip owner and return the deleted participant")
  void removeParticipantByTripOwnerSuccess() {
    Participant participant = this.participants.get(1);
    String userEmail = "user1@email.com";

    when(this.tripRepository.findById(this.trip.getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findByIdAndTripId(participant.getId(), this.trip.getId())).thenReturn(Optional.of(participant));

    Participant deletedParticipant = this.participantService.removeParticipant(this.trip.getId(), participant.getId(), userEmail);

    assertThat(deletedParticipant.getId()).isEqualTo(participant.getId());
    assertThat(deletedParticipant.getName()).isEqualTo(participant.getName());
    assertThat(deletedParticipant.getEmail()).isEqualTo(participant.getEmail());
    assertThat(deletedParticipant.getTrip().getId()).isEqualTo(this.trip.getId());
    assertThat(deletedParticipant.getCreatedAt()).isEqualTo(participant.getCreatedAt());

    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).findByIdAndTripId(participant.getId(), this.trip.getId());
    verify(this.participantRepository, times(1)).deleteById(participant.getId());
  }

  @Test
  @DisplayName("removeParticipant - Should successfully remove a participant as the participant itself and return the deleted participant")
  void removeParticipantByParticipantSuccess() {
    Participant participant = this.participants.get(0);
    String userEmail = "user2@email.com";

    when(this.tripRepository.findById(this.trip.getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findByIdAndTripId(participant.getId(), this.trip.getId())).thenReturn(Optional.of(participant));

    Participant deletedParticipant = this.participantService.removeParticipant(this.trip.getId(), participant.getId(), userEmail);

    assertThat(deletedParticipant.getId()).isEqualTo(participant.getId());
    assertThat(deletedParticipant.getName()).isEqualTo(participant.getName());
    assertThat(deletedParticipant.getEmail()).isEqualTo(participant.getEmail());
    assertThat(deletedParticipant.getTrip().getId()).isEqualTo(this.trip.getId());
    assertThat(deletedParticipant.getCreatedAt()).isEqualTo(participant.getCreatedAt());

    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).findByIdAndTripId(participant.getId(), this.trip.getId());
    verify(this.participantRepository, times(1)).deleteById(participant.getId());
  }

  @Test
  @DisplayName("removeParticipant - Should throw an AccessDeniedException if the user is not the trip owner or the trip participant")
  void removeParticipantFailsByAccessDenied() {
    Participant participant = this.participants.get(0);
    String userEmail = "user3@email.com";

    when(this.tripRepository.findById(this.trip.getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findByIdAndTripId(participant.getId(), this.trip.getId())).thenReturn(Optional.of(participant));

    Exception thrown = catchException(() -> this.participantService.removeParticipant(this.trip.getId(), participant.getId(), userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para remover este recurso");

    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).findByIdAndTripId(participant.getId(), this.trip.getId());
    verify(this.participantRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("removeParticipant - Should throw a RecordNotFoundException if the trip is not found")
  void removeParticipantFailsByTripNotFound() {
    UUID participantId = this.participants.get(0).getId();
    String userEmail = "user1@email.com";

    when(this.tripRepository.findById(this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.participantService.removeParticipant(this.trip.getId(), participantId, userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Viagem de id: '%s' não encontrada", this.trip.getId());

    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, never()).findByIdAndTripId(participantId, this.trip.getId());
    verify(this.participantRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("removeParticipant - Should throw a RecordNotFoundException if the participant is not found")
  void removeParticipantFailsByParticipantNotFound() {
    UUID participantId = this.participants.get(0).getId();
    String userEmail = "user1@email.com";

    when(this.tripRepository.findById(this.trip.getId())).thenReturn(Optional.of(this.trip));
    when(this.participantRepository.findByIdAndTripId(participantId, this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.participantService.removeParticipant(this.trip.getId(), participantId, userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Participante de id: '%s' não encontrado", participantId);

    verify(this.tripRepository, times(1)).findById(this.trip.getId());
    verify(this.participantRepository, times(1)).findByIdAndTripId(participantId, this.trip.getId());
    verify(this.participantRepository, never()).deleteById(any());
  }
}

