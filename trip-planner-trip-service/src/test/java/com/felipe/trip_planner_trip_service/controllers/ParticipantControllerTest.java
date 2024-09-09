package com.felipe.trip_planner_trip_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseInfoDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseTripInfoDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidInviteException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.ParticipantService;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class ParticipantControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ParticipantService participantService;

  private Participant participant;
  private final String BASE_URL = "/api/participants";

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

    this.participant = participant;
  }

  @Test
  @DisplayName("addParticipant - Should return a success response with ok status code and the added participant info")
  void addParticipantSuccess() throws Exception {
    String userEmail = this.participant.getEmail();
    String userId = this.participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");
    String jsonBody = this.objectMapper.writeValueAsString(participantDTO);
    var participantInfoDTO = new ParticipantResponseInfoDTO(this.participant);
    var participantTripInfoDTO = new ParticipantResponseTripInfoDTO(this.participant.getTrip());
    var participantResponseDTO = new AddParticipantResponseDTO(participantInfoDTO, participantTripInfoDTO);

    when(this.participantService.addParticipant(participantDTO, userEmail, userId)).thenReturn(this.participant);

    this.mockMvc.perform(post(BASE_URL + "/confirm")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", userEmail)
      .header("userId", userId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Participante adicionado com sucesso"))
      .andExpect(jsonPath("$.data.participant.id").value(participantResponseDTO.participant().id()))
      .andExpect(jsonPath("$.data.participant.name").value(participantResponseDTO.participant().name()))
      .andExpect(jsonPath("$.data.participant.email").value(participantResponseDTO.participant().email()))
      .andExpect(jsonPath("$.data.trip.id").value(participantResponseDTO.trip().id()))
      .andExpect(jsonPath("$.data.trip.destination").value(participantResponseDTO.trip().destination()))
      .andExpect(jsonPath("$.data.trip.startsAt").value(participantResponseDTO.trip().startsAt()))
      .andExpect(jsonPath("$.data.trip.endsAt").value(participantResponseDTO.trip().endsAt()));

    verify(this.participantService, times(1)).addParticipant(participantDTO, userEmail, userId);
  }

  @Test
  @DisplayName("addParticipant - Should return an error response with not found status code")
  void addParticipantFailsByTripNotFound() throws Exception {
    String userEmail = this.participant.getEmail();
    String userId = this.participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");
    String jsonBody = this.objectMapper.writeValueAsString(participantDTO);

    when(this.participantService.addParticipant(participantDTO, userEmail, userId))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + this.participant.getTrip().getId()  +"' não encontrada"));

    this.mockMvc.perform(post(BASE_URL + "/confirm")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", userEmail)
      .header("userId", userId))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + this.participant.getTrip().getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).addParticipant(participantDTO, userEmail, userId);
  }

  @Test
  @DisplayName("addParticipant - Should return an error response with bad request status code")
  void addParticipantFailsByInvalidInvite() throws Exception {
    String userEmail = this.participant.getEmail();
    String userId = this.participant.getId().toString();
    AddParticipantDTO participantDTO = new AddParticipantDTO("5f1b0d11-07a6-4a63-a5bf-381a09a784af");
    String jsonBody = this.objectMapper.writeValueAsString(participantDTO);

    when(this.participantService.addParticipant(participantDTO, userEmail, userId)).thenThrow(new InvalidInviteException());

    this.mockMvc.perform(post(BASE_URL + "/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonBody)
        .accept(MediaType.APPLICATION_JSON)
        .header("userEmail", userEmail)
        .header("userId", userId))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
      .andExpect(jsonPath("$.message").value("Código de confirmação inválido"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).addParticipant(participantDTO, userEmail, userId);
  }
}
