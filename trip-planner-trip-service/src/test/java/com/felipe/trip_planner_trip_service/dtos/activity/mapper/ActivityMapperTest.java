package com.felipe.trip_planner_trip_service.dtos.activity.mapper;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Activity;
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
public class ActivityMapperTest {

  @Spy
  ActivityMapper activityMapper;

  private List<Activity> activities;

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

    Activity activity2 = new Activity();
    activity2.setId(UUID.fromString("002d3420-7af9-4ea2-9ab8-8afc2fa81da8"));
    activity2.setDescription("Atividade 2");
    activity2.setOwnerEmail("user2@email.com");
    activity2.setTrip(trip);
    activity2.setCreatedAt(mockDateTime);

    this.activities = List.of(activity1, activity2);
  }

  @Test
  @DisplayName("toActivityResponsePageDTO - Should successfully convert a page of Activity into ActivityResponsePageDTO")
  void toActivityResponsePageDTOSuccess() {
    Page<Activity> activities = new PageImpl<>(this.activities);

    ActivityResponsePageDTO convertedActivities = this.activityMapper.toActivityResponsePageDTO(activities);

    assertThat(convertedActivities.totalElements()).isEqualTo(activities.getTotalElements());
    assertThat(convertedActivities.totalPages()).isEqualTo(activities.getTotalPages());
    assertThat(convertedActivities.activities())
      .containsExactlyInAnyOrderElementsOf(activities.getContent().stream().map(ActivityResponseDTO::new).toList());
  }
}
