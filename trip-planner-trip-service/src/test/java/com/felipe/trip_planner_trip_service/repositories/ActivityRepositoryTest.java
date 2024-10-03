package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Activity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class ActivityRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  ActivityRepository activityRepository;

  private List<Activity> activities;
  private List<Trip> trips;

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
    trip2.setStartsAt(LocalDate.parse("24-08-2024", formatter));
    trip2.setEndsAt(LocalDate.parse("26-08-2024", formatter));
    trip2.setCreatedAt(mockDateTime);
    trip2.setUpdatedAt(mockDateTime);

    Activity activity1 = new Activity();
    activity1.setDescription("Atividade 1");
    activity1.setOwnerEmail("user2@email.com");
    activity1.setTrip(trip1);
    activity1.setCreatedAt(mockDateTime);
    activity1.setUpdatedAt(mockDateTime);

    Activity activity2 = new Activity();
    activity2.setDescription("Atividade 2");
    activity2.setOwnerEmail("user2@email.com");
    activity2.setTrip(trip1);
    activity2.setCreatedAt(mockDateTime);
    activity2.setUpdatedAt(mockDateTime);

    Activity activity3 = new Activity();
    activity3.setDescription("Atividade 3");
    activity3.setOwnerEmail("user3@email.com");
    activity3.setTrip(trip1);
    activity3.setCreatedAt(mockDateTime);
    activity3.setUpdatedAt(mockDateTime);

    Activity activity4 = new Activity();
    activity4.setDescription("Atividade 4");
    activity4.setOwnerEmail("user2@email.com");
    activity4.setTrip(trip2);
    activity4.setCreatedAt(mockDateTime);
    activity4.setUpdatedAt(mockDateTime);

    this.activities = List.of(activity1, activity2, activity3, activity4);
    this.trips = List.of(trip1, trip2);
  }

  @Test
  @DisplayName("deleteAllByOwnerEmail - Should successfully delete all activities with the given owner email and trip id")
  void deleteAllByOwnerEmailShouldReturnQuantityOfDeletedActivities() {
    this.entityManager.persist(this.trips.get(0));
    this.entityManager.persist(this.trips.get(1));
    this.entityManager.persist(this.activities.get(0));
    this.entityManager.persist(this.activities.get(1));
    this.entityManager.persist(this.activities.get(2));
    this.entityManager.persist(this.activities.get(3));

    String ownerEmail = "user2@email.com";
    UUID tripId = this.trips.get(0).getId();

    int quantityOfDeletedActivities = this.activityRepository.deleteAllByOwnerEmailAndTripId(ownerEmail, tripId);

    assertThat(quantityOfDeletedActivities).isEqualTo(2);
  }
}
