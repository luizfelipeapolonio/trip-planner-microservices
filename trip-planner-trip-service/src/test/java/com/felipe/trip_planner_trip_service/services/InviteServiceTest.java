package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.clients.UserClient;
import com.felipe.trip_planner_trip_service.dtos.UserClientDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.CreatedInviteDTO;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidInviteException;
import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.InviteRepository;
import com.felipe.trip_planner_trip_service.utils.Actions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    newInvite.setUserId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
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
      this.invite.getUserId().toString(),
      "User 2",
      this.invite.getUserEmail(),
      LocalDateTime.parse("2024-01-01T12:00:00.123456"),
      LocalDateTime.parse("2024-01-01T12:00:00.123456")
    );

    when(this.tripService.checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET))
      .thenReturn(this.trip);
    when(this.userClient.getProfile("user2@email.com")).thenReturn(userClientDTO);
    when(this.inviteRepository.save(any(Invite.class))).thenReturn(this.invite);

    String invitedUserEmail = this.inviteService.invite(this.trip.getId(), "user1@email.com", inviteDTO);

    assertThat(invitedUserEmail).isEqualTo(this.invite.getUserEmail());

    verify(this.tripService, times(1)).checkIfIsTripOwner(this.trip.getId(), "user1@email.com", Actions.GET);
    verify(this.userClient, times(1)).getProfile(inviteDTO.email());
    verify(this.inviteRepository, times(1)).save(any(Invite.class));
    verify(this.kafkaTemplate, times(1)).send(eq("invite"), any(CreatedInviteDTO.class));
  }

  @Test
  @DisplayName("validateInvite - Should successfully validate an invite and not throw any exception")
  void validateInviteSuccess() {
    String inviteCode = this.invite.getCode().toString();
    String userId = this.invite.getUserId().toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Invite validatedInvite = this.inviteService.validateInvite(inviteCode, "user2@email.com", userId);

    assertThat(validatedInvite.getCode()).isEqualTo(this.invite.getCode());
    assertThat(validatedInvite.getTrip().getId()).isEqualTo(this.invite.getTrip().getId());
    assertThat(validatedInvite.getUserId()).isEqualTo(this.invite.getUserId());
    assertThat(validatedInvite.getUsername()).isEqualTo(this.invite.getUsername());
    assertThat(validatedInvite.getUserEmail()).isEqualTo(this.invite.getUserEmail());
    assertThat(validatedInvite.getCreatedAt()).isEqualTo(this.invite.getCreatedAt());

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if an invite is not found")
  void validateInviteFailsByInviteIsNotFound() {
    String inviteCode = this.invite.getCode().toString();
    String userId = this.invite.getUserId().toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, "user2@email.com", userId));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if the the given user e-mail is different from the invited participant e-mail")
  void validateInviteFailsByInvalidUserEmail() {
    String inviteCode = this.invite.getCode().toString();
    String userId = this.invite.getUserId().toString();
    String userEmail = "user3@email.com";

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, userEmail, userId));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if the given user id is different from the invited participant id")
  void validateInviteFailsByInvalidUserId() {
    String inviteCode = this.invite.getCode().toString();
    String userId = UUID.fromString("5f1b0d11-07a6-4a63-a5bf-381a09a784af").toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, "user2@email.com", userId));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }

  @Test
  @DisplayName("validateInvite - Should throw an InvalidInviteException if the given user email and user id is different from invited participant email and id")
  void validateInviteFailsByInvalidUserEmailAndUserId() {
    String inviteCode = this.invite.getCode().toString();
    String userEmail = "user3@email.com";
    String userId = UUID.fromString("5f1b0d11-07a6-4a63-a5bf-381a09a784af").toString();

    when(this.inviteRepository.findByCodeAndIsValidTrue(this.invite.getCode())).thenReturn(Optional.of(this.invite));

    Exception thrown = catchException(() -> this.inviteService.validateInvite(inviteCode, userEmail, userId));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidInviteException.class)
      .hasMessage("Código de confirmação inválido");

    verify(this.inviteRepository, times(1)).findByCodeAndIsValidTrue(this.invite.getCode());
  }
}
