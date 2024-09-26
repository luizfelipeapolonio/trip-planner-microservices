package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateOrUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

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

    Activity activity2 = new Activity();
    activity2.setId(UUID.fromString("002d3420-7af9-4ea2-9ab8-8afc2fa81da8"));
    activity2.setDescription("Atividade 2");
    activity2.setOwnerEmail("user2@email.com");
    activity2.setTrip(trip);
    activity2.setCreatedAt(mockDateTime);

    this.activities = List.of(activity1, activity2);
    this.trip = trip;
  }

  @Test
  @DisplayName("create - Should successfully create an Activity")
  void createActivitySuccess() {
    Activity activity = this.activities.get(0);
    ActivityCreateOrUpdateDTO activityDTO = new ActivityCreateOrUpdateDTO("Atividade 1");

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

  @Test
  @DisplayName("getAllTripActivities - Should successfully get all activities from a trip and return a page of activities")
  void getAllTripActivitiesSuccess() {
    Page<Activity> allActivities = new PageImpl<>(this.activities);
    String userEmail = "user2@email.com";
    Pageable pagination = PageRequest.of(0, 10);

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findAllByTripId(this.trip.getId(), pagination)).thenReturn(allActivities);

    Page<Activity> allTripActivities = this.activityService.getAllTripActivities(this.trip.getId(), userEmail, 0);

    assertThat(allTripActivities.getTotalElements()).isEqualTo(allActivities.getTotalElements());
    assertThat(allTripActivities.getContent())
      .allSatisfy(activity -> assertThat(activity.getTrip().getId()).isEqualTo(this.trip.getId()));

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findAllByTripId(this.trip.getId(), pagination);
  }

  @Test
  @DisplayName("getById - Should successfully get an activity and return it")
  void getByIdSuccess() {
    Activity activity = this.activities.get(0);
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.of(activity));

    Activity foundActivity = this.activityService.getById(this.trip.getId(), activity.getId(), userEmail);

    assertThat(foundActivity.getId()).isEqualTo(activity.getId());
    assertThat(foundActivity.getDescription()).isEqualTo(activity.getDescription());
    assertThat(foundActivity.getOwnerEmail()).isEqualTo(activity.getOwnerEmail());
    assertThat(foundActivity.getTrip().getId()).isEqualTo(activity.getTrip().getId());
    assertThat(foundActivity.getCreatedAt()).isEqualTo(activity.getCreatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
  }

  @Test
  @DisplayName("getById - Should throw a RecordNotFoundException if the activity is not found")
  void getByIdFailsByActivityNotFound() {
    Activity activity = this.activities.get(0);
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.activityService.getById(this.trip.getId(), activity.getId(), userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Atividade de id: '%s' não encontrada", activity.getId());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
  }

  @Test
  @DisplayName("update - The activity owner should be allowed to successfully update the activity")
  void updateBeingActivityOwnerSuccess() {
    Activity activity = this.activities.get(0);
    String userEmail = "user2@email.com";
    var activityDTO = new ActivityCreateOrUpdateDTO("Updated description");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.of(activity));
    when(this.activityRepository.save(activity)).thenReturn(activity);

    Activity updatedActivity = this.activityService.update(this.trip.getId(), activity.getId(), userEmail, activityDTO);

    assertThat(updatedActivity.getId()).isEqualTo(activity.getId());
    assertThat(updatedActivity.getDescription()).isEqualTo(activityDTO.description());
    assertThat(updatedActivity.getOwnerEmail()).isEqualTo(activity.getOwnerEmail());
    assertThat(updatedActivity.getTrip().getId()).isEqualTo(activity.getTrip().getId());
    assertThat(updatedActivity.getCreatedAt()).isEqualTo(activity.getCreatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
    verify(this.activityRepository, times(1)).save(activity);
  }

  @Test
  @DisplayName("update - The trip owner should be allowed to successfully update the activity")
  void updateBeingTripOwnerSuccess() {
    Activity activity = this.activities.get(0);
    String userEmail = "user1@email.com";
    var activityDTO = new ActivityCreateOrUpdateDTO("Updated description");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.of(activity));
    when(this.activityRepository.save(activity)).thenReturn(activity);

    Activity updatedActivity = this.activityService.update(this.trip.getId(), activity.getId(), userEmail, activityDTO);

    assertThat(updatedActivity.getId()).isEqualTo(activity.getId());
    assertThat(updatedActivity.getDescription()).isEqualTo(activityDTO.description());
    assertThat(updatedActivity.getOwnerEmail()).isEqualTo(activity.getOwnerEmail());
    assertThat(updatedActivity.getTrip().getId()).isEqualTo(activity.getTrip().getId());
    assertThat(updatedActivity.getCreatedAt()).isEqualTo(activity.getCreatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
    verify(this.activityRepository, times(1)).save(activity);
  }

  @Test
  @DisplayName("update - Should thrown an AccessDeniedException if the authenticated user is not the trip or the activity owner")
  void updateFailsByUserIsNotActivityOrTripOwner() {
    Activity activity = this.activities.get(0);
    String userEmail = "user3@email.com";
    var activityDTO = new ActivityCreateOrUpdateDTO("Updated description");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.of(activity));

    Exception thrown = catchException(() -> this.activityService.update(this.trip.getId(), activity.getId(), userEmail, activityDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para alterar este recurso");

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
    verify(this.activityRepository, never()).save(any(Activity.class));
  }

  @Test
  @DisplayName("update - Should throw a RecordNotFoundException if the activity is not found")
  void updateFailsByActivityNotFound() {
    Activity activity = this.activities.get(0);
    String userEmail = "user2@email.com";
    var activityDTO = new ActivityCreateOrUpdateDTO("Updated description");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.activityRepository.findByIdAndTripId(activity.getId(), this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.activityService.update(this.trip.getId(), activity.getId(), userEmail, activityDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Atividade de id: '%s' não encontrada", activity.getId());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.activityRepository, times(1)).findByIdAndTripId(activity.getId(), this.trip.getId());
    verify(this.activityRepository, never()).save(any(Activity.class));
  }
}
