package com.felipe.trip_planner_trip_service.dtos.link.mapper;

import com.felipe.trip_planner_trip_service.dtos.link.LinkResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Link;
import com.felipe.trip_planner_trip_service.models.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class LinkMapperTest {

  @Spy
  LinkMapper linkMapper;

  private List<Link> links;

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
  }

  @Test
  @DisplayName("toLinkResponsePageDTO - Should successfully convert a page of Link into LinkResponsePageDTO")
  void toLinkResponsePageDTOSuccess() {
    Page<Link> links = new PageImpl<>(this.links);

    LinkResponsePageDTO convertedLinks = this.linkMapper.toLinkResponsePageDTO(links);

    assertThat(convertedLinks.totalElements()).isEqualTo(links.getTotalElements());
    assertThat(convertedLinks.totalPages()).isEqualTo(links.getTotalPages());
    assertThat(convertedLinks.links())
      .containsExactlyInAnyOrderElementsOf(links.getContent().stream().map(LinkResponseDTO::new).toList());
  }
}
