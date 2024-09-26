package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.link.LinkCreateDTO;
import com.felipe.trip_planner_trip_service.models.Link;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.LinkRepository;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

  @InjectMocks
  LinkService linkService;

  @Mock
  LinkRepository linkRepository;

  @Mock
  TripService tripService;

  private List<Link> links;
  private Trip trip;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    trip.setDestination("Destino 1");
    trip.setOwnerName("User 1");
    trip.setOwnerEmail("user1@email.com");
    trip.setStartsAt(LocalDate.parse("24-08-2024", formatter));
    trip.setEndsAt(LocalDate.parse("26-08-2024", formatter));
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Link link1 = new Link();
    link1.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    link1.setTitle("Link 1");
    link1.setLink("https://somelink.com");
    link1.setOwnerEmail("user2@email.com");
    link1.setCreatedAt(mockDateTime);
    link1.setUpdatedAt(mockDateTime);
    link1.setTrip(trip);

    Link link2 = new Link();
    link2.setId(UUID.fromString("002d3420-7af9-4ea2-9ab8-8afc2fa81da8"));
    link2.setTitle("Link 2");
    link2.setLink("https://somelink2.com");
    link2.setOwnerEmail("user2@email.com");
    link2.setCreatedAt(mockDateTime);
    link2.setUpdatedAt(mockDateTime);
    link2.setTrip(trip);

    this.links = List.of(link1, link2);
    this.trip = trip;
  }

  @Test
  @DisplayName("create - Should successfully create a link and return it")
  void createSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";
    var linkDTO = new LinkCreateDTO("Link 1", "https://somelink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.save(any(Link.class))).thenReturn(link);

    Link createdLink = this.linkService.create(this.trip.getId(), userEmail, linkDTO);

    assertThat(createdLink.getId()).isEqualTo(link.getId());
    assertThat(createdLink.getTitle()).isEqualTo(link.getTitle());
    assertThat(createdLink.getLink()).isEqualTo(link.getLink());
    assertThat(createdLink.getOwnerEmail()).isEqualTo(link.getOwnerEmail());
    assertThat(createdLink.getCreatedAt()).isEqualTo(link.getCreatedAt());
    assertThat(createdLink.getUpdatedAt()).isEqualTo(link.getUpdatedAt());
    assertThat(createdLink.getTrip().getId()).isEqualTo(link.getTrip().getId());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).save(any(Link.class));
  }
}
