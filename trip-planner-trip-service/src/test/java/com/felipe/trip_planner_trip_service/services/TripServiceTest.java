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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    this.trip = trip;
  }

  @Test
  @DisplayName("create - Should successfully create a trip and return it")
  void createTripSuccess() {
    TripDateDTO startsAt = new TripDateDTO("24", "08", "2024");
    TripDateDTO endsAt = new TripDateDTO("26", "08", "2024");
    TripCreateDTO tripDTO = new TripCreateDTO("Destino 1", startsAt, endsAt);

    when(this.tripRepository.save(any(Trip.class))).thenReturn(this.trip);

    Trip newTrip = this.tripService.create("User 1", "user1@email.com", tripDTO);

    assertThat(newTrip.getId()).isEqualTo(this.trip.getId());
    assertThat(newTrip.getDestination()).isEqualTo(this.trip.getDestination());
    assertThat(newTrip.getOwnerName()).isEqualTo(this.trip.getOwnerName());
    assertThat(newTrip.getOwnerEmail()).isEqualTo(this.trip.getOwnerEmail());
    assertThat(newTrip.getStartsAt()).isEqualTo(this.trip.getStartsAt());
    assertThat(newTrip.getEndsAt()).isEqualTo(this.trip.getEndsAt());
    assertThat(newTrip.isConfirmed()).isEqualTo(this.trip.isConfirmed());
    assertThat(newTrip.getCreatedAt()).isEqualTo(this.trip.getCreatedAt());
    assertThat(newTrip.getUpdatedAt()).isEqualTo(this.trip.getUpdatedAt());

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
}
