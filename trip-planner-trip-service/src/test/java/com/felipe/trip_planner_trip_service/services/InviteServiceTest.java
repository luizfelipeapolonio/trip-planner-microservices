package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.clients.UserClient;
import com.felipe.trip_planner_trip_service.dtos.UserClientDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.CreatedInviteDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidInviteException;
import com.felipe.trip_planner_trip_service.exceptions.ParticipantAlreadyExistsException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.utils.Actions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InviteServiceTest {

  @InjectMocks
  InviteService inviteService;

  @Mock
  InviteRepository inviteRepository;

  @Mock
  TripService tripService;

  @Mock
  UserClient userClient;

  @Mock
  KafkaTemplate<String, CreatedInviteDTO> kafkaTemplate;

  private Invite invite;
  private Trip trip;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setId(UUID.fromString("5f1b0d11-07a6-4a63-a5bf-381a09a784af"));
    trip.setDestination("Destino 3");
    trip.setOwnerName("User 1");
    trip.setOwnerEmail("user1@email.com");
    trip.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Invite newInvite = new Invite();
    newInvite.setCode(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    newInvite.setUsername("User 2");
    newInvite.setUserEmail("user2@email.com");
    newInvite.setCreatedAt(mockDateTime);
    newInvite.setTrip(trip);

    this.invite = newInvite;
    this.trip = trip;
  }

  @Test
  @DisplayName("invite - Should successfully create an invite and return the invited user email")
  void inviteSuccess() {
    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    UserClientDTO userClientDTO = new UserClientDTO(
      "77b52d55-3430-4829-a8a4-64ee68336a35",
      "User 2",
      this.invite.getUserEmail(),
      LocalDateTime.parse("2024-01-01T12:00:00.123456"),
      LocalDateTime.parse("2024-01-01T12:00:00.123456")
    );

    when(this.tripService.checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET))
      .thenReturn(this.trip);
    when(this.userClient.getProfile("user2@email.com")).thenReturn(userClientDTO);
    when(this.inviteRepository.findByUserEmailAndTripIdAndIsValidTrue(inviteDTO.email(), this.trip.getId()))
      .thenReturn(Optional.empty());
    when(this.inviteRepository.save(any(Invite.class))).thenReturn(this.invite);

    String invitedUserEmail = this.inviteService.invite(this.trip.getId(), "user1@email.com", inviteDTO);

    assertThat(invitedUserEmail).isEqualTo(this.invite.getUserEmail());

    verify(this.tripService, times(1)).checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET);
    verify(this.userClient, times(1)).getProfile(inviteDTO.email());
    verify(this.inviteRepository, times(1)).findByUserEmailAndTripIdAndIsValidTrue(inviteDTO.email(), this.trip.getId());
    verify(this.inviteRepository, never()).delete(any(Invite.class));
    verify(this.inviteRepository, times(1)).save(any(Invite.class));
    verify(this.kafkaTemplate, times(1)).send(eq("invite"), any(CreatedInviteDTO.class));
  }

  @Test
  @DisplayName("invite - Should delete an existing invite, and successfully create a new invite and return it")
  void inviteDeletingExistingInviteSuccess() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    UserClientDTO userClientDTO = new UserClientDTO(
      "77b52d55-3430-4829-a8a4-64ee68336a35",
      "User 2",
      this.invite.getUserEmail(),
      LocalDateTime.parse("2024-01-01T12:00:00.123456"),
      LocalDateTime.parse("2024-01-01T12:00:00.123456")
    );

    Invite existingInvite = new Invite();
    existingInvite.setCode(UUID.fromString("b610a230-e186-4913-b260-c136f357c75d"));
    existingInvite.setUsername("User 2");
    existingInvite.setUserEmail("user2@email.com");
    existingInvite.setCreatedAt(mockDateTime);
    existingInvite.setTrip(this.trip);

    ArgumentCaptor<Invite> inviteCapture = ArgumentCaptor.forClass(Invite.class);

    when(this.tripService.checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET))
      .thenReturn(this.trip);
    when(this.userClient.getProfile(inviteDTO.email())).thenReturn(userClientDTO);
    when(this.inviteRepository.findByUserEmailAndTripIdAndIsValidTrue(inviteDTO.email(), this.trip.getId()))
      .thenReturn(Optional.of(existingInvite));
    doNothing().when(this.inviteRepository).delete(inviteCapture.capture());
    when(this.inviteRepository.save(any(Invite.class))).thenReturn(this.invite);

    String invitedUserEmail = this.inviteService.invite(this.trip.getId(), "user1@email.com", inviteDTO);

    assertThat(invitedUserEmail).isEqualTo(this.invite.getUserEmail());
    assertThat(inviteCapture.getValue().getCode()).isEqualTo(existingInvite.getCode());
    assertThat(inviteCapture.getValue().getUsername()).isEqualTo(existingInvite.getUsername());
    assertThat(inviteCapture.getValue().getUserEmail()).isEqualTo(existingInvite.getUserEmail());
    assertThat(inviteCapture.getValue().getCreatedAt()).isEqualTo(existingInvite.getCreatedAt());
    assertThat(inviteCapture.getValue().getTrip().getId()).isEqualTo(existingInvite.getTrip().getId());

    verify(this.tripService, times(1)).checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET);
    verify(this.userClient, times(1)).getProfile(inviteDTO.email());
    verify(this.inviteRepository, times(1)).findByUserEmailAndTripIdAndIsValidTrue(inviteDTO.email(), this.trip.getId());
    verify(this.inviteRepository, times(1)).delete(existingInvite);
    verify(this.inviteRepository, times(1)).save(any(Invite.class));
    verify(this.kafkaTemplate, times(1)).send(eq("invite"), any(CreatedInviteDTO.class));
  }

  @Test
  @DisplayName("invite - Should throw a ParticipantAlreadyExistsException if user is a trip participant")
  void inviteFailsByExistingParticipant() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    Participant participant = new Participant();
    participant.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    participant.setName("User 2");
    participant.setEmail("user2@email.com");
    participant.setCreatedAt(mockDateTime);

    this.trip.getParticipants().add(participant);

    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    UserClientDTO userClientDTO = new UserClientDTO(
      "77b52d55-3430-4829-a8a4-64ee68336a35",
      "User 2",
      this.invite.getUserEmail(),
      LocalDateTime.parse("2024-01-01T12:00:00.123456"),
      LocalDateTime.parse("2024-01-01T12:00:00.123456")
    );

    when(this.tripService.checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET))
      .thenReturn(this.trip);
    when(this.userClient.getProfile(inviteDTO.email())).thenReturn(userClientDTO);

    Exception thrown = catchException(() -> this.inviteService.invite(this.trip.getId(), "user1@email.com", inviteDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(ParticipantAlreadyExistsException.class)
      .hasMessage("Usuário de e-mail '%s' já é um participante da viagem", participant.getEmail());

    verify(this.tripService, times(1)).checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET);
    verify(this.userClient, times(1)).getProfile(inviteDTO.email());
    verify(this.inviteRepository, never()).findByUserEmailAndTripIdAndIsValidTrue(any(String.class), any(UUID.class));
    verify(this.inviteRepository, never()).delete(any(Invite.class));
    verify(this.inviteRepository, never()).save(any(Invite.class));
    verify(this.kafkaTemplate, never()).send(any(String.class), any(CreatedInviteDTO.class));
  }

  @Test
  @DisplayName("validateInvite - Should successfully validate an invite and not throw any exception")
  void validateInviteSuccess() {
    String inviteCode = this.invite.getCode().toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Invite validatedInvite = this.inviteService.validateInvite(inviteCode, "user2@email.com");

    assertThat(validatedInvite.getCode()).isEqualTo(this.invite.getCode());
    assertThat(validatedInvite.getTrip().getId()).isEqualTo(this.invite.getTrip().getId());
    assertThat(validatedInvite.getUsername()).isEqualTo(this.invite.getUsername());
    assertThat(validatedInvite.getUserEmail()).isEqualTo(this.invite.getUserEmail());
    assertThat(validatedInvite.getCreatedAt()).isEqualTo(this.invite.getCreatedAt());

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if an invite is not found")
  void validateInviteFailsByInviteIsNotFound() {
    String inviteCode = this.invite.getCode().toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, "user2@email.com"));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if the the given user e-mail is different from the invited participant e-mail")
  void validateInviteFailsByInvalidUserEmail() {
    String inviteCode = this.invite.getCode().toString();
    String userEmail = "user3@email.com";

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }
}
