package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Invite;
import com.felipe.trip_planner_trip_service.models.Trip;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class InviteRepositoryTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private InviteRepository inviteRepository;

  private Trip trip;
  private List<Invite> invites;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setDestination("Destino 1");
    trip.setOwnerName("User 1");
    trip.setOwnerEmail("user1@email.com");
    trip.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Invite invite1 = new Invite();
    //invite1.setCode(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    invite1.setTrip(trip);
    invite1.setUserId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    invite1.setUserEmail("user2@email.com");
    invite1.setCreatedAt(mockDateTime);

    Invite invite2 = new Invite();
    invite2.setTrip(trip);
    invite2.setUserId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    invite2.setUserEmail("user2@email.com");
    invite2.setCreatedAt(mockDateTime);
    invite2.setIsValid(false);

    this.trip = trip;
    this.invites = List.of(invite1, invite2);
  }

  @Test
  @DisplayName("findByCodeAndIsValidTrue - Should successfully return a valid invite")
  void findByCodeAndIsValidTrueSuccess() {
    Invite invite = this.invites.get(0);

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.invites.get(0));
    this.entityManager.persist(this.invites.get(1));

    Optional<Invite> foundInvite = this.inviteRepository.findByCodeAndIsValidTrue(invite.getCode());

    assertThat(foundInvite).isPresent();
    assertThat(foundInvite.get().getCode()).isEqualTo(invite.getCode());
    assertThat(foundInvite.get().isValid()).isTrue();
    assertThat(foundInvite.get().getTrip().getId()).isEqualTo(this.trip.getId());
    assertThat(foundInvite.get().getUserId()).isEqualTo(invite.getUserId());
    assertThat(foundInvite.get().getUserEmail()).isEqualTo(invite.getUserEmail());
    assertThat(foundInvite.get().getCreatedAt()).isEqualTo(invite.getCreatedAt());
  }

  @Test
  @DisplayName("findByCodeAndIsValidTrue - Should return an optional empty if isValid field is equals to false")
  void findByCodeAndIsValidTrueFailsByInvalidInvite() {
    Invite invite = this.invites.get(1);

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.invites.get(0));
    this.entityManager.persist(this.invites.get(1));

    Optional<Invite> foundInvite = this.inviteRepository.findByCodeAndIsValidTrue(invite.getCode());

    assertThat(foundInvite).isEmpty();
  }

  @Test
  @DisplayName("findByCodeAndIsValidTrue - Should return an optional empty if the invite does not exist")
  void findByCodeAndIsValidTrueFailsByInviteNotFound() {
    UUID inviteCode = UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35");

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.invites.get(0));
    this.entityManager.persist(this.invites.get(1));

    Optional<Invite> foundInvite = this.inviteRepository.findByCodeAndIsValidTrue(inviteCode);

    assertThat(foundInvite).isEmpty();
  }
}