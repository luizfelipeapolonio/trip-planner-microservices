package com.felipe.trip_planner_trip_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripDateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripPageResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripResponseDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidDateException;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.TripService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class TripControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  TripService tripService;

  private List<Trip> trips;
  private final String BASE_URL = "/api/trips";

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
    trip.setIsConfirmed(true);
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Trip trip2 = new Trip();
    trip2.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    trip2.setDestination("Destino 2");
    trip2.setOwnerName("User 1");
    trip2.setOwnerEmail("user1@email.com");
    trip2.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip2.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip2.setCreatedAt(mockDateTime);
    trip2.setUpdatedAt(mockDateTime);

    Trip trip3 = new Trip();
    trip3.setId(UUID.fromString("5f1b0d11-07a6-4a63-a5bf-381a09a784af"));
    trip3.setDestination("Destino 3");
    trip3.setOwnerName("User 2");
    trip3.setOwnerEmail("user2@email.com");
    trip3.setStartsAt(LocalDate.parse("02-08-2024", formatter));
    trip3.setEndsAt(LocalDate.parse("05-08-2024", formatter));
    trip3.setCreatedAt(mockDateTime);
    trip3.setUpdatedAt(mockDateTime);

    this.trips = List.of(trip, trip2, trip3);
  }

  @Test
  @DisplayName("create - Should return a success response with created status code and the created trip")
  void createSuccess() throws Exception {
    Trip trip = trips.get(0);
    TripDateDTO startsAt = new TripDateDTO("24", "08", "2024");
    TripDateDTO endsAt = new TripDateDTO("26", "08", "2024");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);
    String jsonBody = this.objectMapper.writeValueAsString(tripDTO);

    when(this.tripService.create("User 1", "user1@email.com", tripDTO)).thenReturn(trip);

    this.mockMvc.perform(post(BASE_URL)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("username", "User 1")
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Viagem criada com sucesso"))
      .andExpect(jsonPath("$.data.id").value(tripResponseDTO.id()))
      .andExpect(jsonPath("$.data.destination").value(tripResponseDTO.destination()))
      .andExpect(jsonPath("$.data.ownerName").value(tripResponseDTO.ownerName()))
      .andExpect(jsonPath("$.data.ownerEmail").value(tripResponseDTO.ownerEmail()))
      .andExpect(jsonPath("$.data.isConfirmed").value(tripResponseDTO.isConfirmed()))
      .andExpect(jsonPath("$.data.startsAt").value(tripResponseDTO.startsAt()))
      .andExpect(jsonPath("$.data.endsAt").value(tripResponseDTO.endsAt()))
      .andExpect(jsonPath("$.data.createdAt").value(tripResponseDTO.createdAt()))
      .andExpect(jsonPath("$.data.updatedAt").value(tripResponseDTO.updatedAt()));

    verify(this.tripService, times(1)).create("User 1", "user1@email.com", tripDTO);
  }

  @Test
  @DisplayName("create - Should return an error response with bad request status code")
  void createFailsByInvalidDate() throws Exception {
    TripDateDTO startsAt = new TripDateDTO("24", "08", "2024");
    TripDateDTO endsAt = new TripDateDTO("22", "08", "2024");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);
    String jsonBody = this.objectMapper.writeValueAsString(tripDTO);

    when(this.tripService.create("User 1", "user1@email.com", tripDTO))
      .thenThrow(new InvalidDateException("A data de término da viagem não pode ser antes da data de início"));

    this.mockMvc.perform(post(BASE_URL)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("username", "User 1")
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
      .andExpect(jsonPath("$.message").value("A data de término da viagem não pode ser antes da data de início"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).create("User 1", "user1@email.com", tripDTO);
  }

  @Test
  @DisplayName("getAllTripsFromAuthUser - Should return a success response with ok status code and all trips from authenticated user")
  void getAllTripsFromAuthUserSuccess() throws Exception {
    Page<Trip> trips = new PageImpl<>(List.of(this.trips.get(0), this.trips.get(1)));
    List<TripResponseDTO> tripDTOs = trips.getContent().stream().map(TripResponseDTO::new).toList();
    TripPageResponseDTO tripPageDTO = new TripPageResponseDTO(tripDTOs, trips.getTotalElements(), trips.getTotalPages());

    CustomResponseBody<TripPageResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as viagens do usuário de email 'user1@email.com'");
    response.setData(tripPageDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.tripService.getAllTripsFromAuthUser("user1@email.com", 0)).thenReturn(trips);

    this.mockMvc.perform(get(BASE_URL + "?page=0")
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.tripService, times(1)).getAllTripsFromAuthUser("user1@email.com", 0);
  }
}
