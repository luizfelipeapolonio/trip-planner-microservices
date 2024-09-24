package com.felipe.trip_planner_trip_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.mapper.ActivityMapper;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.mapper.ParticipantMapper;
import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripDateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripExtraInfoResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripFullResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripPageResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.InvalidDateException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Activity;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.ActivityService;
import com.felipe.trip_planner_trip_service.services.InviteService;
import com.felipe.trip_planner_trip_service.services.ParticipantService;
import com.felipe.trip_planner_trip_service.services.TripService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class TripControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  TripService tripService;

  @MockBean
  InviteService inviteService;

  @MockBean
  ParticipantService participantService;

  @MockBean
  ActivityService activityService;

  @SpyBean
  ParticipantMapper participantMapper;

  @SpyBean
  ActivityMapper activityMapper;

  private List<Trip> trips;
  private List<Participant> participants;
  private List<Activity> activities;
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

    Participant participant = new Participant();
    participant.setId(UUID.fromString("47875e77-5ab5-4386-b266-b8f589bace5a"));
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

    Activity activity1 = new Activity();
    activity1.setId(UUID.fromString("024c61bd-5bbf-445b-9c97-ff0be373d96f"));
    activity1.setDescription("Atividade 1");
    activity1.setOwnerEmail("user2@email.com");
    activity1.setTrip(trip);
    activity1.setCreatedAt(mockDateTime);

    Activity activity2 = new Activity();
    activity2.setId(UUID.fromString("002d3420-7af9-4ea2-9ab8-8afc2fa81da8"));
    activity2.setDescription("Atividade 2");
    activity2.setOwnerEmail("user2@email.com");
    activity2.setTrip(trip);
    activity2.setCreatedAt(mockDateTime);

    this.trips = List.of(trip, trip2, trip3);
    this.participants = List.of(participant, participant2);
    this.activities = List.of(activity1, activity2);
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

  @Test
  @DisplayName("getAllTripsAuthUserIsParticipant - Should return a success response with ok status code and a list of trips")
  void getAllTripsAuthUserIsParticipantSuccess() throws Exception {
    Page<Trip> allTrips = new PageImpl<>(this.trips);
    List<TripResponseDTO> tripResponseDTOs = allTrips.getContent().stream().map(TripResponseDTO::new).toList();
    TripPageResponseDTO tripPageDTO = new TripPageResponseDTO(tripResponseDTOs, allTrips.getTotalElements(), allTrips.getTotalPages());

    CustomResponseBody<TripPageResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as viagens que o usuário de e-mail: 'user2@email.com' é um participante");
    response.setData(tripPageDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.tripService.getAllTripsAuthenticatedUserIsParticipant("user2@email.com", 0)).thenReturn(allTrips);

    this.mockMvc.perform(get(BASE_URL + "/participant")
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user2@email.com"))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.tripService, times(1)).getAllTripsAuthenticatedUserIsParticipant("user2@email.com", 0);
  }

  @Test
  @DisplayName("update - Should return a success response with ok status code and the updated trip")
  void updateSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    TripDateDTO newStartsAt = new TripDateDTO("01", "02", "2024");
    TripDateDTO newEndsAt = new TripDateDTO("02", "02", "2024");
    TripUpdateDTO tripDTO = new TripUpdateDTO("Updated destination", newStartsAt, newEndsAt);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);
    String jsonBody = this.objectMapper.writeValueAsString(tripDTO);

    when(this.tripService.update(trip.getId(), "user1@email.com", tripDTO)).thenReturn(trip);

    this.mockMvc.perform(patch(BASE_URL + "/" + trip.getId())
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Viagem atualizada com sucesso"))
      .andExpect(jsonPath("$.data.id").value(tripResponseDTO.id()))
      .andExpect(jsonPath("$.data.destination").value(tripResponseDTO.destination()))
      .andExpect(jsonPath("$.data.ownerName").value(tripResponseDTO.ownerName()))
      .andExpect(jsonPath("$.data.ownerEmail").value(tripResponseDTO.ownerEmail()))
      .andExpect(jsonPath("$.data.isConfirmed").value(tripResponseDTO.isConfirmed()))
      .andExpect(jsonPath("$.data.startsAt").value(tripResponseDTO.startsAt()))
      .andExpect(jsonPath("$.data.endsAt").value(tripResponseDTO.endsAt()))
      .andExpect(jsonPath("$.data.createdAt").value(tripResponseDTO.createdAt()))
      .andExpect(jsonPath("$.data.updatedAt").value(tripResponseDTO.updatedAt()));

    verify(this.tripService, times(1)).update(trip.getId(), "user1@email.com", tripDTO);
  }

  @Test
  @DisplayName("update - Should return an error response with forbidden status code")
  void updateFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);
    TripDateDTO newStartsAt = new TripDateDTO("01", "02", "2024");
    TripDateDTO newEndsAt = new TripDateDTO("02", "02", "2024");
    TripUpdateDTO tripDTO = new TripUpdateDTO("Updated destination", newStartsAt, newEndsAt);
    String jsonBody = this.objectMapper.writeValueAsString(tripDTO);

    when(this.tripService.update(trip.getId(), "user2@email.com", tripDTO))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(patch(BASE_URL + "/" + trip.getId())
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user2@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).update(trip.getId(), "user2@email.com", tripDTO);
  }

  @Test
  @DisplayName("update - Should return an error response with not found status code")
  void updateFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);
    TripDateDTO newStartsAt = new TripDateDTO("01", "02", "2024");
    TripDateDTO newEndsAt = new TripDateDTO("02", "02", "2024");
    TripUpdateDTO tripDTO = new TripUpdateDTO("Updated destination", newStartsAt, newEndsAt);
    String jsonBody = this.objectMapper.writeValueAsString(tripDTO);

    when(this.tripService.update(trip.getId(), "user1@email.com", tripDTO))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"));

    this.mockMvc.perform(patch(BASE_URL + "/" + trip.getId())
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).update(trip.getId(), "user1@email.com", tripDTO);
  }

  @Test
  @DisplayName("getById - Should return a success response with ok status code and the found trip")
  void getByIdSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    String userEmail = "user1@email.com";
    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);
    Page<Participant> participants = new PageImpl<>(this.participants);
    Page<Activity> activities = new PageImpl<>(this.activities);

    var participantPageDTO = this.participantMapper.toParticipantResponsePageDTO(participants);
    var activitiesPageDTO = this.activityMapper.toActivityResponsePageDTO(activities);
    TripFullResponseDTO tripFullResponseDTO = new TripFullResponseDTO(
      tripResponseDTO,
      new TripExtraInfoResponseDTO(participantPageDTO, activitiesPageDTO)
    );

    CustomResponseBody<TripFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Viagem de id: '" + trip.getId() + "' encontrada");
    response.setData(tripFullResponseDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.tripService.getById(trip.getId(), userEmail)).thenReturn(trip);
    when(this.participantService.getAllTripParticipants(trip.getId(), userEmail, 0)).thenReturn(participants);
    when(this.activityService.getAllTripActivities(trip.getId(), userEmail, 0)).thenReturn(activities);

    this.mockMvc.perform(get(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.tripService, times(1)).getById(trip.getId(), userEmail);
    verify(this.participantService, times(1)).getAllTripParticipants(trip.getId(), userEmail, 0);
    verify(this.participantMapper, times(2)).toParticipantResponsePageDTO(participants);
  }

  @Test
  @DisplayName("getById - Should return an error response with not found status code")
  void getByIdFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);

    when(this.tripService.getById(trip.getId(), "user1@email.com"))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"));

    this.mockMvc.perform(get(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).getById(trip.getId(), "user1@email.com");
    verify(this.participantService, never()).getAllTripParticipants(any(UUID.class), anyString(), anyInt());
    verify(this.participantMapper, never()).toParticipantResponsePageDTO(any());
  }

  @Test
  @DisplayName("getById - Should return an error response with forbidden status code")
  void getByIdFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);

    when(this.tripService.getById(trip.getId(), "user1@email.com"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).getById(trip.getId(), "user1@email.com");
    verify(this.participantService, never()).getAllTripParticipants(any(UUID.class), anyString(), anyInt());
    verify(this.participantMapper, never()).toParticipantResponsePageDTO(any());
  }

  @Test
  @DisplayName("delete - Should return a success response with ok status code and the deleted trip")
  void deleteSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);

    when(this.tripService.delete(trip.getId(), "user1@email.com")).thenReturn(trip);

    this.mockMvc.perform(delete(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' excluída com sucesso"))
      .andExpect(jsonPath("$.data.deletedTrip.id").value(tripResponseDTO.id()))
      .andExpect(jsonPath("$.data.deletedTrip.destination").value(tripResponseDTO.destination()))
      .andExpect(jsonPath("$.data.deletedTrip.ownerName").value(tripResponseDTO.ownerName()))
      .andExpect(jsonPath("$.data.deletedTrip.ownerEmail").value(tripResponseDTO.ownerEmail()))
      .andExpect(jsonPath("$.data.deletedTrip.isConfirmed").value(tripResponseDTO.isConfirmed()))
      .andExpect(jsonPath("$.data.deletedTrip.startsAt").value(tripResponseDTO.startsAt()))
      .andExpect(jsonPath("$.data.deletedTrip.endsAt").value(tripResponseDTO.endsAt()))
      .andExpect(jsonPath("$.data.deletedTrip.createdAt").value(tripResponseDTO.createdAt()))
      .andExpect(jsonPath("$.data.deletedTrip.updatedAt").value(tripResponseDTO.updatedAt()));

    verify(this.tripService, times(1)).delete(trip.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("delete - Should return an error response with not found status code")
  void deleteFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);

    when(this.tripService.delete(trip.getId(), "user1@email.com"))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"));

    this.mockMvc.perform(delete(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).delete(trip.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("delete - Should return an error response with forbidden status code")
  void deleteFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);

    when(this.tripService.delete(trip.getId(), "user1@email.com"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para excluir este recurso"));

    this.mockMvc.perform(delete(BASE_URL + "/" + trip.getId())
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para excluir este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).delete(trip.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("deleteAllTripsFromAuthUser - Should return a success response with ok status code and the quantity of deleted trips")
  void deleteAllTripsFromAuthUserSuccess() throws Exception {
    List<Trip> allTrips = List.of(this.trips.get(0), this.trips.get(1));

    when(this.tripService.deleteAllTripsFromAuthUser("user1@email.com")).thenReturn(allTrips.size());

    this.mockMvc.perform(delete(BASE_URL)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Todas as viagens do usuário de email: 'user1@email.com' foram excluídas com sucesso"))
      .andExpect(jsonPath("$.data").value("Quantidade de viagens excluídas: " + allTrips.size()));

    verify(this.tripService, times(1)).deleteAllTripsFromAuthUser("user1@email.com");
  }

  @Test
  @DisplayName("confirmTrip - Should return a success response with ok status code")
  void confirmTripTrueSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/confirm", BASE_URL, trip.getId());

    doNothing().when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("A viagem de id: '" + trip.getId() + "' foi confirmada com sucesso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);
  }

  @Test
  @DisplayName("confirmTrip - Should return an error response with forbidden status code")
  void confirmTripFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/confirm", BASE_URL, trip.getId());

    doThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"))
      .when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);
  }

  @Test
  @DisplayName("confirmTrip - Should return an error response with not found status code")
  void confirmTripFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/confirm", BASE_URL, trip.getId());

    doThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", true);
  }

  @Test
  @DisplayName("cancelTrip - Should return a success response with ok status code")
  void cancelTripSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/cancel", BASE_URL, trip.getId());

    doNothing().when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("A viagem de id: '" + trip.getId() + "' foi cancelada com sucesso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);
  }

  @Test
  @DisplayName("cancelTrip - Should throw an error response with forbidden status code")
  void cancelTripFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/cancel", BASE_URL, trip.getId());

    doThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"))
      .when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);
  }

  @Test
  @DisplayName("cancelTrip - Should return an error response with not found status code")
  void cancelTripFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);
    String url = String.format("%s/%s/cancel", BASE_URL, trip.getId());

    doThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .when(this.tripService).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);

    this.mockMvc.perform(patch(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.tripService, times(1)).confirmOrCancelTrip(trip.getId(), "user1@email.com", false);
  }

  @Test
  @DisplayName("inviteParticipant - Should return a success response with ok status code")
  void inviteParticipantSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    String url = String.format("%s/%s/invite", BASE_URL, trip.getId());
    String jsonBody = this.objectMapper.writeValueAsString(inviteDTO);

    when(this.inviteService.invite(trip.getId(), "user1@email.com", inviteDTO)).thenReturn(inviteDTO.email());

    this.mockMvc.perform(post(url)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Convite enviado com sucesso para: user2@email.com"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.inviteService, times(1)).invite(trip.getId(), "user1@email.com", inviteDTO);
  }

  @Test
  @DisplayName("inviteParticipant - Should return an error response with not found status code")
  void inviteParticipantFailsByTripNotFound() throws Exception {
    Trip trip = this.trips.get(0);
    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    String url = String.format("%s/%s/invite", BASE_URL, trip.getId());
    String jsonBody = this.objectMapper.writeValueAsString(inviteDTO);

    when(this.inviteService.invite(trip.getId(), "user1@email.com", inviteDTO))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + trip.getId() + "' não encontrada"));

    this.mockMvc.perform(post(url)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + trip.getId() + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.inviteService, times(1)).invite(trip.getId(), "user1@email.com", inviteDTO);
  }

  @Test
  @DisplayName("inviteParticipant - Should return an error response with forbidden status code")
  void inviteParticipantFailsByAccessDenied() throws Exception {
    Trip trip = this.trips.get(0);
    InviteParticipantDTO inviteDTO = new InviteParticipantDTO("user2@email.com");
    String url = String.format("%s/%s/invite", BASE_URL, trip.getId());
    String jsonBody = this.objectMapper.writeValueAsString(inviteDTO);

    when(this.inviteService.invite(trip.getId(), "user1@email.com", inviteDTO))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(post(url)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath(".message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.inviteService, times(1)).invite(trip.getId(), "user1@email.com", inviteDTO);
  }

  @Test
  @DisplayName("getAllTripParticipants - Should return a success response with ok status code and a Page with all trip participants")
  void getAllTripParticipantsSuccess() throws Exception {
    Trip trip = this.trips.get(0);
    Page<Participant> allParticipants = new PageImpl<>(this.participants);

    List<ParticipantResponseDTO> participantDTOs = allParticipants.getContent()
      .stream()
      .map(ParticipantResponseDTO::new)
      .toList();

    var participantResponsePageDTO = new ParticipantResponsePageDTO(
      participantDTOs,
      allParticipants.getTotalElements(),
      allParticipants.getTotalPages()
    );

    CustomResponseBody<ParticipantResponsePageDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os participantes da viagem de id: '" + trip.getId() + "'");
    response.setData(participantResponsePageDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);
    String url = String.format("%s/%s/participants", BASE_URL, trip.getId());

    when(this.participantService.getAllTripParticipants(trip.getId(), "user1@email.com", 0))
      .thenReturn(allParticipants);
    when(this.participantMapper.toParticipantResponsePageDTO(allParticipants)).thenReturn(participantResponsePageDTO);

    this.mockMvc.perform(get(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.participantService, times(1)).getAllTripParticipants(trip.getId(), "user1@email.com", 0);
    verify(this.participantMapper, times(1)).toParticipantResponsePageDTO(allParticipants);
  }

  @Test
  @DisplayName("getAllTripParticipants - Should return an error response with not found status code")
  void getAllTripParticipantsFailsByTripNotFound() throws Exception {
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/participants", BASE_URL, tripId);

    when(this.participantService.getAllTripParticipants(tripId, "user1@email.com", 0))
      .thenThrow(new RecordNotFoundException("Viagem de id: '" + tripId + "' não encontrada"));

    this.mockMvc.perform(get(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Viagem de id: '" + tripId + "' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).getAllTripParticipants(tripId, "user1@email.com", 0);
    verify(this.participantMapper, never()).toParticipantResponsePageDTO(any());
  }

  @Test
  @DisplayName("getAllTripParticipants - Should return an error response with forbidden status code")
  void getAllTripParticipantsFailsByAccessDenied() throws Exception {
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/participants", BASE_URL, tripId);

    when(this.participantService.getAllTripParticipants(tripId, "user1@email.com", 0))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).getAllTripParticipants(tripId, "user1@email.com", 0);
    verify(this.participantMapper, never()).toParticipantResponsePageDTO(any());
  }

  @Test
  @DisplayName("removeParticipant - Should return a success response with ok status code and the removed participant")
  void removeParticipantSuccess() throws Exception {
    Participant participant = this.participants.get(0);
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/participants/%s", BASE_URL, tripId, participant.getId());
    var participantResponseDTO = new ParticipantResponseDTO(participant);

    when(this.participantService.removeParticipant(tripId, participant.getId(), "user1@email.com"))
      .thenReturn(participant);

    this.mockMvc.perform(delete(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Participante removido com sucesso"))
      .andExpect(jsonPath("$.data.removedParticipant.id").value(participantResponseDTO.id()))
      .andExpect(jsonPath("$.data.removedParticipant.name").value(participantResponseDTO.name()))
      .andExpect(jsonPath("$.data.removedParticipant.email").value(participantResponseDTO.email()))
      .andExpect(jsonPath("$.data.removedParticipant.tripId").value(participantResponseDTO.tripId()))
      .andExpect(jsonPath("$.data.removedParticipant.createdAt").value(participantResponseDTO.createdAt()));

    verify(this.participantService, times(1)).removeParticipant(tripId, participant.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("removeParticipant - Should return an error response with forbidden status code")
  void removeParticipantFailsByAccessDenied() throws Exception {
    Participant participant = this.participants.get(0);
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/participants/%s", BASE_URL, tripId, participant.getId());

    when(this.participantService.removeParticipant(tripId, participant.getId(), "user1@email.com"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso"));

    this.mockMvc.perform(delete(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para remover este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).removeParticipant(tripId, participant.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("removeParticipant - Should return a RecordNotFoundException with not found status code")
  void removeParticipantFailsByParticipantNotFound() throws Exception {
    Participant participant = this.participants.get(0);
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/participants/%s", BASE_URL, tripId, participant.getId());

    when(this.participantService.removeParticipant(tripId, participant.getId(), "user1@email.com"))
      .thenThrow(new RecordNotFoundException("Participante de id: '" + participant.getId() + "' não encontrado"));

    this.mockMvc.perform(delete(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user1@email.com"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Participante de id: '" + participant.getId() + "' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.participantService, times(1)).removeParticipant(tripId, participant.getId(), "user1@email.com");
  }

  @Test
  @DisplayName("createActivity - Should return a success response with created status code and the created activity")
  void createActivitySuccess() throws Exception {
    Activity activity = this.activities.get(0);
    Trip trip = this.trips.get(0);
    ActivityCreateDTO activityDTO = new ActivityCreateDTO("Atividade 1");
    ActivityResponseDTO activityResponseDTO = new ActivityResponseDTO(activity);

    String url = String.format("%s/%s/activities", BASE_URL, trip.getId());
    String jsonBody = this.objectMapper.writeValueAsString(activityDTO);

    when(this.activityService.create(trip.getId(), "user2@email.com", activityDTO)).thenReturn(activity);

    this.mockMvc.perform(post(url)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonBody)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user2@email.com"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Atividade criada com sucesso"))
      .andExpect(jsonPath("$.data.id").value(activityResponseDTO.id()))
      .andExpect(jsonPath("$.data.description").value(activityResponseDTO.description()))
      .andExpect(jsonPath("$.data.tripId").value(activityResponseDTO.tripId()))
      .andExpect(jsonPath("$.data.ownerEmail").value(activityResponseDTO.ownerEmail()))
      .andExpect(jsonPath("$.data.createdAt").value(activityResponseDTO.createdAt()));

    verify(this.activityService, times(1)).create(trip.getId(), "user2@email.com", activityDTO);
  }

  @Test
  @DisplayName("getAllTripActivities - Should return a success response with ok status code and a page of activities")
  void getAllTripActivitiesSuccess() throws Exception {
    UUID tripId = this.trips.get(0).getId();
    String url = String.format("%s/%s/activities?page=%d", BASE_URL, tripId, 0);
    Page<Activity> allActivities = new PageImpl<>(this.activities);
    List<ActivityResponseDTO> activityResponseDTOs = allActivities.getContent()
      .stream()
      .map(ActivityResponseDTO::new)
      .toList();
    var activityResponsePageDTO = new ActivityResponsePageDTO(
      activityResponseDTOs,
      allActivities.getTotalElements(),
      allActivities.getTotalPages()
    );

    CustomResponseBody<ActivityResponsePageDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as atividades da viagem de id: '" + tripId + "'");
    response.setData(activityResponsePageDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.activityService.getAllTripActivities(tripId, "user2@email.com", 0)).thenReturn(allActivities);
    when(this.activityMapper.toActivityResponsePageDTO(allActivities)).thenReturn(activityResponsePageDTO);

    this.mockMvc.perform(get(url)
      .accept(MediaType.APPLICATION_JSON)
      .header("userEmail", "user2@email.com"))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.activityService, times(1)).getAllTripActivities(tripId, "user2@email.com", 0);
    verify(this.activityMapper, times(1)).toActivityResponsePageDTO(allActivities);
  }
}
