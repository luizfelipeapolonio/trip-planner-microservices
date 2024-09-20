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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class TripRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  TripRepository tripRepository;

  private List<Trip> trips;
  private List<Participant> participants;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip1 = new Trip();
    trip1.setDestination("Destino 1");
    trip1.setOwnerName("User 1");
    trip1.setOwnerEmail("user1@email.com");
    trip1.setStartsAt(LocalDate.parse("24-08-2024", formatter));
    trip1.setEndsAt(LocalDate.parse("26-08-2024", formatter));
    trip1.setCreatedAt(mockDateTime);
    trip1.setUpdatedAt(mockDateTime);

    Trip trip2 = new Trip();
    trip2.setDestination("Destino 2");
    trip2.setOwnerName("User 1");
    trip2.setOwnerEmail("user1@email.com");
    trip2.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip2.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip2.setCreatedAt(mockDateTime);
    trip2.setUpdatedAt(mockDateTime);

    Trip trip3 = new Trip();
    trip3.setDestination("Destino 3");
    trip3.setOwnerName("User 1");
    trip3.setOwnerEmail("user1@email.com");
    trip3.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip3.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip3.setCreatedAt(mockDateTime);
    trip3.setUpdatedAt(mockDateTime);

    Participant participant1 = new Participant();
    participant1.setName("User 3");
    participant1.setEmail("user3@email.com");
    participant1.setCreatedAt(mockDateTime);
    participant1.setTrip(trip1);

    Participant participant2 = new Participant();
    participant2.setName("User 3");
    participant2.setEmail("user3@email.com");
    participant2.setCreatedAt(mockDateTime);
    participant2.setTrip(trip2);

    Participant participant3 = new Participant();
    participant3.setName("User 3");
    participant3.setEmail("user2@email.com");
    participant3.setCreatedAt(mockDateTime);
    participant3.setTrip(trip3);

    this.trips = List.of(trip1, trip2, trip3);
    this.participants = List.of(participant1, participant2, participant3);
  }

  @Test
  @DisplayName("findAllByParticipantEmail - Should successfully return all trips that authenticated user is a participant")
  void findAllByParticipantEmailReturnsAllTrips() {
    this.entityManager.persist(this.trips.get(0));
    this.entityManager.persist(this.trips.get(1));
    this.entityManager.persist(this.trips.get(2));
    this.entityManager.persist(this.participants.get(0));
    this.entityManager.persist(this.participants.get(1));
    this.entityManager.persist(this.participants.get(2));

    String participantEmail = "user3@email.com";
    UUID trip1Id = this.trips.get(0).getId();
    UUID trip2Id = this.trips.get(1).getId();
    Pageable pageable = PageRequest.of(0, 10);

    Page<Trip> allParticipantTrips = this.tripRepository.findAllByParticipantEmail(participantEmail, pageable);

    assertThat(allParticipantTrips.getTotalElements()).isEqualTo(2L);
    assertThat(allParticipantTrips.getContent().stream().map(Trip::getId).toList()).containsOnly(trip1Id, trip2Id);
  }

  @Test
  @DisplayName("findAllByParticipantEmail - Should return an empty list if no trips are found")
  void findAllByParticipantEmailReturnsEmptyTripsList() {
    this.entityManager.persist(this.trips.get(0));
    this.entityManager.persist(this.trips.get(1));
    this.entityManager.persist(this.trips.get(2));
    this.entityManager.persist(this.participants.get(0));
    this.entityManager.persist(this.participants.get(1));
    this.entityManager.persist(this.participants.get(2));

    String userEmail = "user4@email.com";
    Pageable pageable = PageRequest.of(0, 10);

    Page<Trip> allParticipantTrips = this.tripRepository.findAllByParticipantEmail(userEmail, pageable);

    assertThat(allParticipantTrips.getContent()).isEmpty();
  }
}
