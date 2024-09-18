package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class ParticipantRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  ParticipantRepository participantRepository;

  private Participant participant;
  private Trip trip;

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

    Participant participant = new Participant();
    participant.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    participant.setName("User 2");
    participant.setEmail("user2@email.com");
    participant.setCreatedAt(mockDateTime);
    participant.setTrip(trip);

    this.participant = participant;
    this.trip = trip;
  }

  @Test
  @DisplayName("findByEmailAndTripId - Should successfully return a participant of a trip, given the user email and trip id")
  void findByEmailAndTripIdReturnsParticipant() {
    String userEmail = "user2@email.com";

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.participant);

    UUID tripId = this.trip.getId();

    Optional<Participant> foundParticipant = this.participantRepository.findByEmailAndTripId(userEmail, tripId);

    assertThat(foundParticipant).isPresent();
    assertThat(foundParticipant.get().getId()).isEqualTo(this.participant.getId());
    assertThat(foundParticipant.get().getName()).isEqualTo(this.participant.getName());
    assertThat(foundParticipant.get().getEmail()).isEqualTo(this.participant.getEmail());
    assertThat(foundParticipant.get().getCreatedAt()).isEqualTo(this.participant.getCreatedAt());
    assertThat(foundParticipant.get().getTrip().getId()).isEqualTo(this.participant.getTrip().getId());
  }

  @Test
  @DisplayName("findByEmailAndTripId - Should return an optional empty if the participant is not found")
  void findByEmailAndTripIdReturnsEmptyByParticipantNotFound() {
    String userEmail = "user3@email.com";

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.participant);

    UUID tripId = this.trip.getId();

    Optional<Participant> foundParticipant = this.participantRepository.findByEmailAndTripId(userEmail, tripId);

    assertThat(foundParticipant).isEmpty();
  }

  @Test
  @DisplayName("findByIdAndTripId - Should successfully return a participant of a trip, given the id and trip id")
  void findByIdAndTripIdReturnsParticipant() {
    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.participant);

    UUID tripId = this.trip.getId();
    UUID participantId = this.participant.getId();

    Optional<Participant> foundParticipant = this.participantRepository.findByIdAndTripId(participantId, tripId);

    assertThat(foundParticipant).isPresent();
    assertThat(foundParticipant.get().getId()).isEqualTo(this.participant.getId());
    assertThat(foundParticipant.get().getName()).isEqualTo(this.participant.getName());
    assertThat(foundParticipant.get().getEmail()).isEqualTo(this.participant.getEmail());
    assertThat(foundParticipant.get().getCreatedAt()).isEqualTo(this.participant.getCreatedAt());
    assertThat(foundParticipant.get().getTrip().getId()).isEqualTo(this.participant.getTrip().getId());
  }

  @Test
  @DisplayName("findByIdAndTripId - Should return an optional empty if the participant is not found")
  void findByIdAndTripIdReturnsEmptyByParticipantNotFound() {
    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.participant);

    UUID tripId = this.trip.getId();
    UUID participantId = UUID.fromString("5f1b0d11-07a6-4a63-a5bf-381a09a784af");

    Optional<Participant> foundParticipant = this.participantRepository.findByIdAndTripId(participantId, tripId);

    assertThat(foundParticipant).isEmpty();
  }

  @Test
  @DisplayName("findAllAndTripId - Should return a Page of Participant with all trip participants")
  void findAllByTripIdReturnsAllParticipants() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    Participant participant2 = new Participant();
    participant2.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    participant2.setName("User 3");
    participant2.setEmail("user3@email.com");
    participant2.setCreatedAt(mockDateTime);
    participant2.setTrip(this.trip);

    Pageable pageable = PageRequest.of(0, 10);

    this.entityManager.persist(this.trip);
    this.entityManager.persist(this.participant);
    this.entityManager.persist(participant2);

    Page<Participant> returnedPage = this.participantRepository.findAllByTripId(this.trip.getId(), pageable);

    assertThat(returnedPage.getTotalElements()).isEqualTo(2L);
    assertThat(returnedPage.getContent())
      .allSatisfy(participant -> assertThat(participant.getTrip().getId()).isEqualTo(this.trip.getId()));
  }
}
