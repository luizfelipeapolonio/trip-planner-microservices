package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateDTO;
import com.felipe.trip_planner_trip_service.models.Activity;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.ActivityRepository;
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
public class ActivityServiceTest {

  @InjectMocks
  ActivityService activityService;

  @Mock
  ActivityRepository activityRepository;

  @Mock
  TripService tripService;

  private List<Activity> activities;
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

    Activity activity1 = new Activity();
    activity1.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    activity1.setDescription("Atividade 1");
    activity1.setOwnerEmail("user2@email.com");
    activity1.setTrip(trip);
    activity1.setCreatedAt(mockDateTime);

    this.activities = List.of(activity1);
    this.trip = trip;
  }

  @Test
  @DisplayName("create - Should successfully create an Activity")
  void createActivitySuccess() {
    Activity activity = this.activities.get(0);
    ActivityCreateDTO activityDTO = new ActivityCreateDTO("Atividade 1");

    when(this.tripService.getById(this.trip.getId(), "user2@email.com")).thenReturn(this.trip);
    when(this.activityRepository.save(any(Activity.class))).thenReturn(activity);

    Activity createdActivity = this.activityService.create(this.trip.getId(), "user2@email.com", activityDTO);

    assertThat(createdActivity.getId()).isEqualTo(activity.getId());
    assertThat(createdActivity.getDescription()).isEqualTo(activityDTO.description());
    assertThat(createdActivity.getOwnerEmail()).isEqualTo(activity.getOwnerEmail());
    assertThat(createdActivity.getTrip().getId()).isEqualTo(this.trip.getId());
    assertThat(createdActivity.getCreatedAt()).isEqualTo(activity.getCreatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), "user2@email.com");
    verify(this.activityRepository, times(1)).save(any(Activity.class));
  }
}
