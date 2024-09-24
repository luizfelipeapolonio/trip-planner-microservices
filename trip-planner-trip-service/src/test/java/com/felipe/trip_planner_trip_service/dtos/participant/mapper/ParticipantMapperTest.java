package com.felipe.trip_planner_trip_service.dtos.participant.mapper;

import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Participant;
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
public class ParticipantMapperTest {

  @Spy
  ParticipantMapper participantMapper;

  private List<Participant> participants;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
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

    Participant participant2 = new Participant();
    participant2.setId(UUID.fromString("b610a230-e186-4913-b260-c136f357c75d"));
    participant2.setName("User 3");
    participant2.setEmail("user3@email.com");
    participant2.setCreatedAt(mockDateTime);
    participant2.setTrip(trip);

    this.participants = List.of(participant, participant2);
  }

  @Test
  @DisplayName("toParticipantResponsePageDTO - Should successfully convert a page of participants into a ParticipantResponsePageDTO")
  void toParticipantResponsePageDTOSuccess() {
    Page<Participant> participantsPage = new PageImpl<>(this.participants);

    ParticipantResponsePageDTO participantsPageDTO = this.participantMapper.toParticipantResponsePageDTO(participantsPage);

    assertThat(participantsPageDTO.totalElements()).isEqualTo(participantsPage.getTotalElements());
    assertThat(participantsPageDTO.totalPages()).isEqualTo(participantsPage.getTotalPages());
    assertThat(participantsPageDTO.participants().size()).isEqualTo(participantsPage.getContent().size());
    assertThat(participantsPageDTO.participants())
      .containsExactlyInAnyOrderElementsOf(participantsPage.getContent().stream().map(ParticipantResponseDTO::new).toList());
  }
}
