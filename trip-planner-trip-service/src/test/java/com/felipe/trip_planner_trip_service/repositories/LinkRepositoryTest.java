package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Link;
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
public class LinkRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  LinkRepository linkRepository;

  private List<Link> links;
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

    Link link1 = new Link();
    link1.setTitle("Link 1");
    link1.setUrl("https://somelink.com");
    link1.setOwnerEmail("user2@email.com");
    link1.setCreatedAt(mockDateTime);
    link1.setUpdatedAt(mockDateTime);
    link1.setTrip(trip1);

    Link link2 = new Link();
    link2.setTitle("Link 2");
    link2.setUrl("https://somelink2.com");
    link2.setOwnerEmail("user2@email.com");
    link2.setCreatedAt(mockDateTime);
    link2.setUpdatedAt(mockDateTime);
    link2.setTrip(trip1);

    Link link3 = new Link();
    link3.setTitle("Link 3");
    link3.setUrl("https://somelink3.com");
    link3.setOwnerEmail("user3@email.com");
    link3.setCreatedAt(mockDateTime);
    link3.setUpdatedAt(mockDateTime);
    link3.setTrip(trip1);

    Link link4 = new Link();
    link4.setTitle("Link 4");
    link4.setUrl("https://somelink4.com");
    link4.setOwnerEmail("user2@email.com");
    link4.setCreatedAt(mockDateTime);
    link4.setUpdatedAt(mockDateTime);
    link4.setTrip(trip2);

    this.links = List.of(link1, link2, link3, link4);
    this.trips = List.of(trip1, trip2);
  }

  @Test
  @DisplayName("deleteAllByOwnerEmailAndTripId - Should successfully all links with the given owner email and trip id")
  void deleteAllByOwnerEmailAndTripIdShouldReturnQuantityOfDeletedLinks() {
    this.entityManager.persist(this.trips.get(0));
    this.entityManager.persist(this.trips.get(1));
    this.entityManager.persist(this.links.get(0));
    this.entityManager.persist(this.links.get(1));
    this.entityManager.persist(this.links.get(2));
    this.entityManager.persist(this.links.get(3));

    String ownerEmail = "user2@email.com";
    UUID tripId = this.trips.get(0).getId();

    int quantityOfDeletedLinks = this.linkRepository.deleteAllByOwnerEmailAndTripId(ownerEmail, tripId);

    assertThat(quantityOfDeletedLinks).isEqualTo(2);
  }
}
