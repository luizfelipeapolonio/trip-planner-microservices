package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Participant;
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
}
