package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripDateDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidDateException;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

  @InjectMocks
  TripService tripService;

  @Mock
  TripRepository tripRepository;

  private List<Trip> trips;

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
  @DisplayName("create - Should successfully create a trip and return it")
  void createTripSuccess() {
    Trip trip = this.trips.get(0);
    TripDateDTO startsAt = new TripDateDTO("24", "08", "2024");
    TripDateDTO endsAt = new TripDateDTO("26", "08", "2024");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);

    when(this.tripRepository.save(any(Trip.class))).thenReturn(trip);

    Trip newTrip = this.tripService.create("User 1", "user1@email.com", tripDTO);

    assertThat(newTrip.getId()).isEqualTo(trip.getId());
    assertThat(newTrip.getDestination()).isEqualTo(trip.getDestination());
    assertThat(newTrip.getOwnerName()).isEqualTo(trip.getOwnerName());
    assertThat(newTrip.getOwnerEmail()).isEqualTo(trip.getOwnerEmail());
    assertThat(newTrip.getStartsAt()).isEqualTo(trip.getStartsAt());
    assertThat(newTrip.getEndsAt()).isEqualTo(trip.getEndsAt());
    assertThat(newTrip.isConfirmed()).isEqualTo(trip.isConfirmed());
    assertThat(newTrip.getCreatedAt()).isEqualTo(trip.getCreatedAt());
    assertThat(newTrip.getUpdatedAt()).isEqualTo(trip.getUpdatedAt());

    verify(this.tripRepository, times(1)).save(any(Trip.class));
  }

  @Test
  @DisplayName("create - Should throw an InvalidDateException if is not possible to convert the string value to date")
  void createTripFailsByInvalidDate() {
    TripDateDTO startsAt = new TripDateDTO("aa", "aa", "aaaa");
    TripDateDTO endsAt = new TripDateDTO("aa", "aa", "aaaa");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);

    Exception thrown = catchException(() -> this.tripService.create("User 1", "user1@email.com", tripDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidDateException.class)
      .hasMessage("Data inválida! Não foi possível converter o valor 'aa-aa-aaaa' em data");

    verify(this.tripRepository, never()).save(any(Trip.class));
  }

  @Test
  @DisplayName("create - Should throw an InvalidDateException if the ending date is before starting date")
  void createTripFailsByEndingDateIsBeforeStartsDate() {
    TripDateDTO startsAt = new TripDateDTO("02", "01", "2024");
    TripDateDTO endsAt = new TripDateDTO("01", "01", "2024");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);

    Exception thrown = catchException(() -> this.tripService.create("User 1", "user1@email.com", tripDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidDateException.class)
      .hasMessage("A data de término da viagem não pode ser antes da data de início");

    verify(this.tripRepository, never()).save(any(Trip.class));
  }

  @Test
  @DisplayName("getAllAuthUserTrips - Should successfully return a Page object with all trips of authenticated user")
  void getAllAuthUserTripsSuccess() {
    String ownerEmail = "user1@email.com";
    Page<Trip> trips = new PageImpl<>(List.of(this.trips.get(0), this.trips.get(1)));
    Pageable pagination = PageRequest.of(0, 10);

    when(this.tripRepository.findAllByOwnerEmail(ownerEmail, pagination)).thenReturn(trips);

    Page<Trip> allTrips = this.tripService.getAllTripsFromAuthUser(ownerEmail, 0);

    assertThat(allTrips.getTotalElements()).isEqualTo(trips.getTotalElements());
    assertThat(allTrips.getContent().stream().map(Trip::getOwnerEmail).toList())
      .containsExactlyInAnyOrderElementsOf(trips.getContent().stream().map(Trip::getOwnerEmail).toList());

    verify(this.tripRepository, times(1)).findAllByOwnerEmail(ownerEmail, pagination);
  }
}
